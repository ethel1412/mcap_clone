package nic.meg.mcap.controllers.pageControllers;

import lombok.RequiredArgsConstructor;
import nic.meg.mcap.dto.response.AllProgrammeResponseDTO;
import nic.meg.mcap.dto.response.ProgrammeResponseDTO;
import nic.meg.mcap.entities.AdmissionWindow;
import nic.meg.mcap.entities.Programme;
import nic.meg.mcap.entities.ProgrammeOffered;
import nic.meg.mcap.entities.Stream;
import nic.meg.mcap.repositories.AdmissionWindowRepository;
import nic.meg.mcap.repositories.ProgrammeRepository;
import nic.meg.mcap.repositories.ProgrammesOfferedRepository;
import nic.meg.mcap.repositories.ScheduleRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

import java.util.*;

@Controller
@RequestMapping("/controller/admissions")
@RequiredArgsConstructor
public class ManageAdmissionsPageController {

    private final AdmissionWindowRepository admissionWindowRepository;
    private final ProgrammesOfferedRepository programmesOfferedRepository;
    private final ProgrammeRepository programmeRepository;
    private final ScheduleRepository scheduleRepository;

    @GetMapping("/programmes/all")
    @ResponseBody
    public List<AllProgrammeResponseDTO> getAllProgrammes() {
        return programmeRepository.getAllProgrammes();
    }
    
    @Validated
    @GetMapping("/manage")
    public String manage(

            @RequestParam("admissionWindowCode")
            @Pattern(
                    regexp = "^[A-Za-z0-9_-]{1,50}$",
                    message = "Invalid admission window code"
                )
            String admissionWindowCode,

            @RequestParam(value = "roundType", required = false)
            String roundType,

            @RequestParam(value = "phaseNo", required = false)

            @Min(value = 1, message = "Invalid phase number")

            @Max(value = 20, message = "Invalid phase number")
            Integer phaseNo,

            Model model) {

        AdmissionWindow window = admissionWindowRepository.findByAdmissionCode(admissionWindowCode)
                .orElseThrow(() -> new IllegalArgumentException("Admission window not found with code: " + admissionWindowCode));

        Short admissionWindowId = window.getAdmissionId();

        // 1. DYNAMICALLY Fetch Rounds (Map COMBINED to UI Tabs)
        List<String> rawRounds = scheduleRepository.findDistinctRoundsForWindow(admissionWindowId);
        List<String> availableRounds = new ArrayList<>();

        // --- NEW: Detect Combined Route ---
        boolean isCombinedRoute = rawRounds.contains("COMBINED");

        if (rawRounds.isEmpty() || rawRounds.contains("CUET") || isCombinedRoute) {
            availableRounds.add("CUET");
        }
        if (rawRounds.contains("NON_CUET") || isCombinedRoute) {
            availableRounds.add("NON_CUET");
        }

        // Determine Active Round (Tab)
        String rt = (roundType == null) ? availableRounds.get(0) : roundType.toUpperCase(Locale.ROOT);
        if (!availableRounds.contains(rt)) {
            rt = availableRounds.get(0); // Safely fallback
        }

        // 2. DYNAMICALLY Fetch Phases for this Tab (Crucial: Include COMBINED phases!)
        List<String> targetRoutes = Arrays.asList(rt, "COMBINED");
        List<Integer> availablePhases = scheduleRepository.findDistinctPhasesForWindowAndRoutes(admissionWindowId, targetRoutes);

        if (availablePhases.isEmpty()) {
            availablePhases = List.of(1); // Failsafe
        }

        // Determine Active Phase (Pill)
        int ph = (phaseNo == null || phaseNo < 1) ? availablePhases.get(0) : phaseNo;
        if (!availablePhases.contains(ph)) {
            ph = availablePhases.get(0);
        }
        int maxPhaseForRound = Collections.max(availablePhases);
        java.time.LocalDateTime phaseStartTime = scheduleRepository.findStartDateForPhase(admissionWindowId, targetRoutes, ph);
        boolean isPhaseOpen = true;
        String formattedStartTime = "Date not set";

        if (phaseStartTime != null) {
            if (java.time.LocalDateTime.now().isBefore(phaseStartTime)) {
                isPhaseOpen = false;
            }
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");
            formattedStartTime = phaseStartTime.format(formatter);
        }

        model.addAttribute("isPhaseOpen", isPhaseOpen);
        model.addAttribute("phaseStartTime", formattedStartTime);

        // Fetch Programmes
        Map<Short, Map<String, Object>> unique = new LinkedHashMap<>();
        List<ProgrammeOffered> offerings = findOfferingsForWindow(window);

        for (ProgrammeOffered po : offerings) {
            Short pId = po.getProgramme().getProgrammeId();
            unique.computeIfAbsent(pId, id -> {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("programmeId", pId);
                row.put("programmeName", po.getProgramme().getProgrammeName());
                // Include stream info per-programme so the UI can filter
                Stream progStream = po.getProgramme().getStream();
                row.put("streamId", progStream != null ? progStream.getStreamId() : null);
                row.put("streamName", progStream != null ? progStream.getStreamName() : "All Streams");
                return row;
            });
        }
        model.addAttribute("admissionWindowCode", admissionWindowCode);
        model.addAttribute("streamName", window.getStream() != null ? window.getStream().getStreamName() : "All Streams");
        model.addAttribute("isAllStreams", window.getStream() == null);
        model.addAttribute("programmeLevel", window.getProgrammeLevel() != null ? window.getProgrammeLevel().toString() : "UG");
        model.addAttribute("admissionSession", window.getSession());

        // Distinct streams for the filter UI (used only in All Streams / FYUG windows)
        List<Map<String, Object>> distinctStreams = unique.values().stream()
                .filter(r -> r.get("streamId") != null)
                .collect(java.util.stream.Collectors.toMap(
                        r -> r.get("streamId"),
                        r -> r,
                        (a, b) -> a,
                        java.util.LinkedHashMap::new))
                .values().stream()
                .map(r -> Map.<String, Object>of(
                        "streamId", r.get("streamId"),
                        "streamName", r.get("streamName")))
                .collect(java.util.stream.Collectors.toList());
        model.addAttribute("distinctStreams", distinctStreams);
        model.addAttribute("programmes", new ArrayList<>(unique.values()));

        // Pass dynamic roadmap to HTML
        model.addAttribute("roundType", rt);
        model.addAttribute("phaseNo", ph);
        model.addAttribute("availableRounds", availableRounds);
        model.addAttribute("availablePhases", availablePhases);
        model.addAttribute("maxPhaseForRound", maxPhaseForRound);

        // --- NEW: Pass the boolean flag to Thymeleaf ---
        model.addAttribute("isCombinedRoute", isCombinedRoute);

        return "controller/admissions/manage-admissions";
    }

    @GetMapping("/merit-list")
    public String meritList(
            @RequestParam("admissionWindowCode") String admissionWindowCode,
            @RequestParam("programmeId") Short programmeId,
            @RequestParam(value = "roundType", required = false, defaultValue = "CUET") String roundType,
            @RequestParam(value = "phaseNo", required = false, defaultValue = "1") Integer phaseNo,
            Model model) {

        AdmissionWindow window = admissionWindowRepository.findByAdmissionCode(admissionWindowCode)
                .orElseThrow(() -> new IllegalArgumentException("Admission window not found with code: " + admissionWindowCode));
        Programme programme = programmeRepository.findById(programmeId).orElseThrow();

        model.addAttribute("admissionWindowCode", admissionWindowCode);
        model.addAttribute("programmeId", programmeId);
        model.addAttribute("programmeName", programme.getProgrammeName());
        model.addAttribute("roundType", roundType.toUpperCase());
        model.addAttribute("phaseNo", phaseNo);
        model.addAttribute("streamId", window.getStream() != null ? window.getStream().getStreamId() : null);
        model.addAttribute("streamName", window.getStream() != null ? window.getStream().getStreamName() : "All Streams");
        model.addAttribute("programmeLevel", window.getProgrammeLevel() != null ? window.getProgrammeLevel().toString() : "UG");
        model.addAttribute("admissionSession", window.getSession());

        return "controller/admissions/merit-list";
    }

    /**
     * Returns ProgrammeOffered rows for a window.
     * Specific-stream window → filter by that stream.
     * All Streams window (stream == null) → fetch by programme level across all streams.
     */
    private List<nic.meg.mcap.entities.ProgrammeOffered> findOfferingsForWindow(
            nic.meg.mcap.entities.AdmissionWindow window) {
        if (window.getStream() != null && window.getStream().getStreamId() != null) {
            return programmesOfferedRepository.findByStreamProgramme(
                    List.of(window.getStream().getStreamId()),
                    window.getProgrammeLevel()
            );
        }
        return (List<nic.meg.mcap.entities.ProgrammeOffered>)
                programmesOfferedRepository.findByProgramme_ProgrammeLevel(window.getProgrammeLevel());
    }
}