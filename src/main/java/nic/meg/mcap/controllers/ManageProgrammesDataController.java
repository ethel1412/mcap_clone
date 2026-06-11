package nic.meg.mcap.controllers;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import nic.meg.mcap.dto.response.AdmissionWindowProgrammeRowDTO;
import nic.meg.mcap.dto.response.ProgrammeOfferedResponseDTO;
import nic.meg.mcap.dto.response.UpcomingAdmissionWindowResponseDTO;
import nic.meg.mcap.entities.AdmissionWindow;
import nic.meg.mcap.entities.AdmissionWindowProgramme;
import nic.meg.mcap.entities.Institute;
import nic.meg.mcap.entities.InstituteAdmissionPreference;
import nic.meg.mcap.entities.Schedule;
import nic.meg.mcap.repositories.AdmissionWindowProgrammeRepository;
import nic.meg.mcap.repositories.AdmissionWindowRepository;
import nic.meg.mcap.repositories.InstituteAdmissionPreferenceRepository;
import nic.meg.mcap.repositories.InstituteRepository;
import nic.meg.mcap.repositories.ScheduleRepository;
import nic.meg.mcap.services.AdmissionWindowService;
import nic.meg.mcap.services.InstituteService;
import nic.meg.mcap.services.ProgrammeOfferedService;
import nic.meg.mcap.services.SeatMatrixService;
import nic.meg.mcap.services.UpcomingAdmissionWindowQueryService;

@RestController
@RequestMapping("/manage-programmes-data")
public class ManageProgrammesDataController {

    // EXACT MATCH WITH THE SMART TIMELINE
    private static final String SEAT_LOCKING_STEP_NAME = "Institutes Lock Seats & Programmes";
    private static final Logger logger = LoggerFactory.getLogger(ManageProgrammesDataController.class);

    @Autowired
    private UpcomingAdmissionWindowQueryService upcomingAdmissionWindowQueryService;
    @Autowired
    private final ProgrammeOfferedService programmeOfferedService;
    @Autowired
    private final SeatMatrixService seatMatrixService;
    @Autowired
    private final AdmissionWindowService admissionWindowService;
    @Autowired
    private final InstituteService instituteService;
    @Autowired
    private final AdmissionWindowProgrammeRepository admissionWindowProgrammeRepository;
    @Autowired
    private final ScheduleRepository scheduleRepository;
    @Autowired
    private AdmissionWindowRepository admissionWindowRepository;
    @Autowired
    private InstituteAdmissionPreferenceRepository instituteAdmissionPreferenceRepository;
    @Autowired
    private InstituteRepository instituteRepository;

    public ManageProgrammesDataController(ProgrammeOfferedService programmeOfferedService,
                                          SeatMatrixService seatMatrixService, AdmissionWindowService admissionWindowService,
                                          InstituteService instituteService, AdmissionWindowProgrammeRepository admissionWindowProgrammeRepository,
                                          ScheduleRepository scheduleRepository) {
        this.programmeOfferedService = programmeOfferedService;
        this.seatMatrixService = seatMatrixService;
        this.admissionWindowService = admissionWindowService;
        this.instituteService = instituteService;
        this.admissionWindowProgrammeRepository = admissionWindowProgrammeRepository;
        this.scheduleRepository = scheduleRepository;
    }

    // CHANGED: admissionId to admissionCode
    @GetMapping("/institute/admission-window/{admissionCode}/programmes")
    public ResponseEntity<List<AdmissionWindowProgrammeRowDTO>> getProgrammesForAdmissionWindow(
            @PathVariable("admissionCode") String admissionCode, Principal principal) {

        // CHANGED: Fetch by code, extract internal ID
        AdmissionWindow window = admissionWindowService.findByCode(admissionCode);
        Short admissionId = window.getAdmissionId();

        Short instituteId = instituteService.findInstituteIdByUsername(principal.getName());

        boolean hasSpecificProgrammes = admissionWindowProgrammeRepository
                .existsByAdmissionWindowAdmissionId(admissionId);

        List<ProgrammeOfferedResponseDTO> offeredList;

        if (hasSpecificProgrammes) {
            List<AdmissionWindowProgramme> awpList = admissionWindowProgrammeRepository
                    .findByAdmissionWindowAdmissionId(admissionId);

            Set<Short> programmeIds = awpList.stream().map(awp -> awp.getProgramme().getProgrammeId())
                    .collect(Collectors.toSet());

            offeredList = programmeOfferedService.listProgrammesByInstituteAndProgrammeIds(instituteId, programmeIds);
        } else {
            var windowLevel = window.getProgrammeLevel().name();

            Short windowStreamId = (window.getStream() != null) ? window.getStream().getStreamId() : null;

            offeredList = programmeOfferedService.listProgrammesByInstitute(instituteId).stream()
                    .filter(po -> po.getProgrammeLevel().equals(windowLevel))
                    .filter(po -> windowStreamId == null || po.getStreamId().equals(windowStreamId))
                    .collect(Collectors.toList());
        }

        List<AdmissionWindowProgrammeRowDTO> rows = offeredList.stream().map(po -> {
            Integer poId = po.getProgrammeOfferedId();
            var matrixOpt = seatMatrixService.getSeatMatrixByProgrammeOfferedId(poId);

            int totalSeats = 0;
            String status = "DRAFT";

            if (matrixOpt.isPresent()) {
                totalSeats = matrixOpt.get().getTotalSeats();
                status = matrixOpt.get().getApprovalStatus();
                if (status == null) status = "DRAFT";
            }

            boolean hasValidSeats = totalSeats > 0;
            boolean alreadySent = "SUBMITTED".equals(status);

            String shiftCode = (po.getShift() != null) ? po.getShift().name() : "NA";
            String shiftName = (po.getShiftDisplayName() != null) ? po.getShiftDisplayName() : "Not Applicable";

            // Fetch institute-level CUET preference for this window (same for all rows)
            boolean wantsCuet = instituteAdmissionPreferenceRepository
                    .findByInstituteInstituteIdAndAdmissionWindowAdmissionId(instituteId, admissionId)
                    .map(InstituteAdmissionPreference::isWantsCuet)
                    .orElse(false);

            return new AdmissionWindowProgrammeRowDTO(poId, po.getProgrammeName(),
                    (window.getStream() != null ? window.getStream().getStreamName() : "All Streams"),
                    window.getProgrammeLevel().name(), totalSeats, 0, hasValidSeats, true, alreadySent,
                    status, shiftCode, shiftName, wantsCuet);
        }).collect(Collectors.toList());

        return ResponseEntity.ok(rows);
    }

    // CHANGED: admissionId to admissionCode
    @PostMapping("/institute/admission-window/{admissionCode}/send-for-approval")
    public ResponseEntity<?> sendSeatsForApproval(@PathVariable("admissionCode") String admissionCode,
                                                  @RequestBody Map<String, List<Integer>> payload, Principal principal) {

        List<Integer> programmeOfferedIds = payload.get("programmeOfferedIds");

        if (programmeOfferedIds == null || programmeOfferedIds.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "No programmes selected"));
        }
        AdmissionWindow window = admissionWindowService.findByCode(admissionCode);
        Short admissionId = window.getAdmissionId();

        // SECURITY CHECK: Get ID and pass it down to verify ownership
        Short loggedInInstituteId = instituteService.findInstituteIdByUsername(principal.getName());
        seatMatrixService.sendForApproval(admissionId, programmeOfferedIds, loggedInInstituteId);

        return ResponseEntity.ok(Map.of("message", "Seat Matrix finally submitted and locked successfully."));
    }

    // CHANGED: admissionId to admissionCode
    @GetMapping("/institute/admission-window/{admissionCode}/schedule-status")
    public ResponseEntity<Map<String, Object>> getScheduleStatus(@PathVariable("admissionCode") String admissionCode) {

        // CHANGED: Fetch by code, extract internal ID
        AdmissionWindow window = admissionWindowService.findByCode(admissionCode);
        Short admissionId = window.getAdmissionId();

        LocalDateTime now = LocalDateTime.now();

        Optional<Schedule> scheduleOpt = scheduleRepository.findByAdmissionWindowIdAndStepName(admissionId,
                SEAT_LOCKING_STEP_NAME);

        Map<String, Object> response = new HashMap<>();

        if (scheduleOpt.isPresent()) {
            Schedule schedule = scheduleOpt.get();

            boolean isWithinDateRange = !now.isBefore(schedule.getStartDate()) && !now.isAfter(schedule.getEndDate());

            response.put("isActive", isWithinDateRange);
            response.put("startDate", schedule.getStartDate().toString());
            response.put("endDate", schedule.getEndDate().toString());
            response.put("stepName", schedule.getStepName());

            if (isWithinDateRange) {
                response.put("message", "Seat management is currently active");
            } else if (now.isBefore(schedule.getStartDate())) {
                response.put("message", "Seat management window has not started yet. Opens on "
                        + schedule.getStartDate().toLocalDate());
            } else {
                response.put("message",
                        "Seat management window has closed. Ended on " + schedule.getEndDate().toLocalDate());
            }

        } else {
            response.put("isActive", false);
            response.put("message", "Schedule not configured for seat management");
            response.put("stepName", SEAT_LOCKING_STEP_NAME);
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/institute/windows")
    public ResponseEntity<?> getInstituteWindows() {
        List<AdmissionWindow> allWindows = admissionWindowRepository.findAll();
        List<Map<String, Object>> validWindows = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (AdmissionWindow w : allWindows) {
            Optional<Schedule> scheduleOpt = scheduleRepository.findByAdmissionWindowIdAndStepName(w.getAdmissionId(),
                    SEAT_LOCKING_STEP_NAME);

            String scheduleStatus;
            LocalDateTime schStart = null;
            LocalDateTime schEnd = null;

            if (scheduleOpt.isEmpty()) {
                scheduleStatus = "NOT_SCHEDULED";
            } else {
                Schedule sch = scheduleOpt.get();
                schStart = sch.getStartDate();
                schEnd = sch.getEndDate();

                if (now.isBefore(sch.getStartDate())) {
                    scheduleStatus = "UPCOMING";
                } else if (now.isAfter(sch.getEndDate())) {
                    scheduleStatus = "CLOSED";
                } else {
                    scheduleStatus = "OPEN";
                }
            }

            validWindows.add(Map.of(
                    // CHANGED: Expose admissionCode instead of admissionId
                    "admissionCode", w.getAdmissionCode(), "streamName",
                    (w.getStream() != null ? w.getStream().getStreamName() : "--"), "programmeLevel",
                    w.getProgrammeLevel().name(), "session", w.getSession(), "scheduleStart",
                    (schStart != null) ? schStart.toString() : "N/A", "scheduleEnd",
                    (schEnd != null) ? schEnd.toString() : "N/A", "scheduleStatus", scheduleStatus));
        }

        return ResponseEntity.ok(validWindows);
    }

    @GetMapping("/institute/upcoming-windows")
    public List<UpcomingAdmissionWindowResponseDTO> getUpcomingWindowsForInstitute() {
        return upcomingAdmissionWindowQueryService.findUpcomingAdmissionWindow();
    }

    /**
     * GET: Returns the saved CUET preference for the logged-in institute for a given window.
     * Response: { "wantsCuet": true/false, "submitted": true/false }
     */
    @GetMapping("/institute/admission-window/{admissionCode}/cuet-preference")
    public ResponseEntity<Map<String, Object>> getCuetPreference(
            @PathVariable("admissionCode") String admissionCode,
            Principal principal) {

        AdmissionWindow window = admissionWindowService.findByCode(admissionCode);
        Short instituteId = instituteService.findInstituteIdByUsername(principal.getName());

        var prefOpt = instituteAdmissionPreferenceRepository
                .findByInstituteInstituteIdAndAdmissionWindowAdmissionId(instituteId, window.getAdmissionId());

        Map<String, Object> result = new HashMap<>();
        result.put("wantsCuet", prefOpt.map(InstituteAdmissionPreference::isWantsCuet).orElse(false));
        result.put("submitted", prefOpt.map(InstituteAdmissionPreference::isPreferenceSubmitted).orElse(false));
        return ResponseEntity.ok(result);
    }

    /**
     * POST: Saves the CUET participation preference for the logged-in institute for a given window.
     * Blocked once preferenceSubmitted = true (locked by Final Submit).
     *
     * CSRF is enforced by Spring Security on all non-exempt POST endpoints.
     * The CSRF token is injected into axios via the meta tags in layout.html.
     */
    @PostMapping("/institute/admission-window/{admissionCode}/cuet-preference")
    public ResponseEntity<?> saveCuetPreference(
            @PathVariable("admissionCode") String admissionCode,
            @RequestBody Map<String, Boolean> body,
            Principal principal) {

        AdmissionWindow window = admissionWindowService.findByCode(admissionCode);
        Short instituteId = instituteService.findInstituteIdByUsername(principal.getName());

        var prefOpt = instituteAdmissionPreferenceRepository
                .findByInstituteInstituteIdAndAdmissionWindowAdmissionId(instituteId, window.getAdmissionId());

        // Prevent change after Final Submit
        if (prefOpt.isPresent() && prefOpt.get().isPreferenceSubmitted()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "CUET preference is locked after Final Submit and cannot be changed."));
        }

        Boolean wantsCuet = body.get("wantsCuet");
        if (wantsCuet == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "wantsCuet field is required."));
        }

        Institute institute = instituteRepository.findById(instituteId)
                .orElseThrow(() -> new RuntimeException("Institute not found"));

        InstituteAdmissionPreference pref = prefOpt
                .orElseGet(() -> new InstituteAdmissionPreference(institute, window, false));

        pref.setWantsCuet(wantsCuet);
        instituteAdmissionPreferenceRepository.save(pref);

        return ResponseEntity.ok(Map.of(
                "message", "CUET preference saved successfully.",
                "wantsCuet", pref.isWantsCuet()));
    }
}