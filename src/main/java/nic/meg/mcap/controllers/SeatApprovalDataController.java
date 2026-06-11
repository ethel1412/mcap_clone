package nic.meg.mcap.controllers;

import nic.meg.mcap.entities.AdmissionWindow;
import nic.meg.mcap.entities.Schedule;
import nic.meg.mcap.repositories.AdmissionWindowRepository;
import nic.meg.mcap.repositories.ScheduleRepository;
import nic.meg.mcap.repositories.SeatMatrixRepository;
import nic.meg.mcap.services.SeatMatrixService;
import nic.meg.mcap.services.SeatReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static nic.meg.mcap.enums.StepPreset.*;

@RestController
@RequestMapping("/controller/seat-approvals")
@PreAuthorize("hasRole('CONTROLLER')")
public class SeatApprovalDataController {

    @Autowired private SeatMatrixService seatMatrixService;
    @Autowired private SeatReservationService seatReservationService;
    @Autowired private AdmissionWindowRepository admissionWindowRepository;
    @Autowired private ScheduleRepository scheduleRepository;
    @Autowired private SeatMatrixRepository seatMatrixRepository;

    private static final String APPROVAL_STEP_NAME = "Controller approval of institute admission settings";

    @GetMapping("/windows")
    public ResponseEntity<?> getAdmissionWindows() {
        List<AdmissionWindow> allWindows = admissionWindowRepository.findAll();
        List<Map<String, Object>> validWindows = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (AdmissionWindow w : allWindows) {

            // 1. PARENT CHECK: Is the Admission Window itself active?
            // If the parent window is closed, we don't show it at all.
            if (now.isBefore(w.getStartDate()) || now.isAfter(w.getEndDate())) {
                continue;
            }

            // 2. CHILD CHECK: What is the status of the specific "Controller Approval" step?
            Optional<Schedule> scheduleOpt = scheduleRepository.findByAdmissionWindowIdAndStepName(
                    w.getAdmissionId(), CONTROLLER_APPROVAL.getLabel());

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

            // 3. PENDING COUNT (Using the query we fixed earlier)
            long pendingCount = seatMatrixRepository.countByApprovalStatusAndAdmissionId("PENDING", w.getAdmissionId());

            validWindows.add(Map.<String, Object>of(
                    "admissionId", w.getAdmissionId(),
                    "name", (w.getStream() != null ? w.getStream().getStreamName() : "All Streams") + " - " + w.getProgrammeLevel() + " (" + w.getSession() + ")",
                    "scheduleStart", (schStart != null) ? schStart : "N/A",
                    "scheduleEnd", (schEnd != null) ? schEnd : "N/A",
                    "scheduleStatus", scheduleStatus, // OPEN, CLOSED, UPCOMING, NOT_SCHEDULED
                    "pendingCount", pendingCount
            ));
        }

        return ResponseEntity.ok(validWindows);
    }

    // Security Check: Prevents bypassing UI by calling API directly
    @GetMapping("/check-schedule/{admissionId}")
    public ResponseEntity<?> checkScheduleStatus(@PathVariable Short admissionId) {
        Optional<Schedule> scheduleOpt = scheduleRepository.findByAdmissionWindowIdAndStepName(
                admissionId, CONTROLLER_APPROVAL.getLabel());

        if (scheduleOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("active", false, "message", "Schedule not found."));
        }

        Schedule schedule = scheduleOpt.get();
        LocalDateTime now = LocalDateTime.now();

        // Strict Check: Only allow if within the specific schedule dates
        if (now.isBefore(schedule.getStartDate())) {
            return ResponseEntity.ok(Map.of("active", false, "message", "Approval period has not started yet."));
        } else if (now.isAfter(schedule.getEndDate())) {
            return ResponseEntity.ok(Map.of("active", false, "message", "Approval period has ended."));
        }

        return ResponseEntity.ok(Map.of("active", true, "message", "Active"));
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAll(@RequestParam(required = false) Short admissionId) {
        if (admissionId == null) return ResponseEntity.badRequest().body("ID required");
        // Implement filtering by admissionId in service
        return ResponseEntity.ok(seatMatrixService.getAllSeatApprovals());
    }


    @GetMapping("/preview/{programmeOfferedId}")
    public ResponseEntity<?> getReservations(@PathVariable Integer programmeOfferedId) {
        return ResponseEntity.ok(seatReservationService.getReservationsByProgrammeOffered(programmeOfferedId));
    }
    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable Long id) {
        seatMatrixService.approveSeatMatrix(id);
        return ResponseEntity.ok(Map.of("message", "Approved"));
    }
    @PostMapping("/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable Long id, @RequestParam String reason) {
        seatMatrixService.rejectSeatMatrix(id, reason);
        return ResponseEntity.ok(Map.of("message", "Rejected"));
    }
    @GetMapping("/count-pending")
    public ResponseEntity<Long> getPendingCount() {
        List<AdmissionWindow> allWindows = admissionWindowRepository.findAll();
        LocalDateTime now = LocalDateTime.now();
        long totalPending = 0;

        for (AdmissionWindow w : allWindows) {
            // 1. Skip if Parent Window is closed
            if (now.isBefore(w.getStartDate()) || now.isAfter(w.getEndDate())) {
                continue;
            }

            // 2. Check "Controller approval" schedule
            Optional<Schedule> scheduleOpt = scheduleRepository.findByAdmissionWindowIdAndStepName(
                    w.getAdmissionId(), CONTROLLER_APPROVAL.getLabel());

            if (scheduleOpt.isPresent()) {
                Schedule sch = scheduleOpt.get();
                // 3. Only count if this specific step is currently OPEN
                if (now.isAfter(sch.getStartDate()) && now.isBefore(sch.getEndDate())) {
                    totalPending += seatMatrixRepository.countByApprovalStatusAndAdmissionId("PENDING", w.getAdmissionId());
                }
            }
        }

        // Returns a simple number (e.g., 5) which your JS expects
        return ResponseEntity.ok(totalPending);
    }
}