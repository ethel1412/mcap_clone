package nic.meg.mcap.controllers;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nic.meg.mcap.dto.request.SeatAllotmentDecisionRequestDTO;
import nic.meg.mcap.dto.request.SubjectPreferenceRequestDTO;
import nic.meg.mcap.dto.response.CounselingRoundResponseDTO;
import nic.meg.mcap.dto.response.SeatAllotmentResponseDTO;
import nic.meg.mcap.dto.response.SubjectPreferenceResponseDTO;
import nic.meg.mcap.services.CounselingService;

@RestController
@RequestMapping("/api/applicants/counseling")
@RequiredArgsConstructor
public class CounselingDataController {

    private static final Logger logger = LoggerFactory.getLogger(CounselingDataController.class);
    private final CounselingService counselingService;

    // =========================================================================
    // 1. SIDEBAR OVERVIEW (Runs on Login/Refresh)
    // =========================================================================
    @GetMapping("/overviews")
    public ResponseEntity<List<CounselingRoundResponseDTO>> getApplicantAllotmentOverviews(
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.emptyList());
        }

        String applicantNo = authentication.getName();
        List<CounselingRoundResponseDTO> overviews = counselingService.getApplicantAllotmentOverviews(applicantNo);
        return ResponseEntity.ok(overviews);
    }

    // =========================================================================
    // 2. FETCH ALLOTMENT (With Auto-Recovery for Blank Screens)
    // =========================================================================
    @GetMapping("/allotment")
    public ResponseEntity<SeatAllotmentResponseDTO> getAllotmentForWindow(
            @RequestParam(value = "admissionWindowId", required = false) Short admissionWindowId,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String applicantNo = authentication.getName();
        SeatAllotmentResponseDTO dto;

        // FIX: If windowId is missing (after logout/login), fetch the most recent
        // active allotment
        if (admissionWindowId == null) {
            dto = counselingService.getLatestSeatAllotment(applicantNo);
        } else {
            dto = counselingService.getSeatAllotmentForWindow(applicantNo, admissionWindowId);
        }

        if (dto == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(dto);
    }

    // =========================================================================
    // 3. DECISION ACTIONS (Accept/Reject)
    // =========================================================================
    @PostMapping("/accept")
    public ResponseEntity<Map<String, Object>> acceptAllotment(
            @Valid @RequestBody SeatAllotmentDecisionRequestDTO requestDTO, Authentication authentication) {
        String applicantNo = authentication.getName();
        try {
            counselingService.acceptAllotment(applicantNo, requestDTO.getAllotmentId());

            return ResponseEntity.ok(
                    Map.of("message", "Allotment accepted successfully.", "allotmentId", requestDTO.getAllotmentId()));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Invalid request"));
        }
    }

    @PostMapping("/reject")
    public ResponseEntity<Map<String, Object>> rejectAllotment(
            @Valid @RequestBody SeatAllotmentDecisionRequestDTO requestDTO, Authentication authentication) {
        String applicantNo = authentication.getName();
        try {
            counselingService.rejectAllotment(applicantNo, requestDTO.getAllotmentId());

            return ResponseEntity.ok(Map.of("message", "Offer rejected successfully."));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Invalid request"));
        }
    }

    @PostMapping("/slide-up")
    public ResponseEntity<Map<String, Object>> slideUpAllotment(
            @Valid @RequestBody SeatAllotmentDecisionRequestDTO requestDTO, Authentication authentication) {
        String applicantNo = authentication.getName();
        try {
            counselingService.slideUpAllotment(applicantNo, requestDTO.getAllotmentId());

            return ResponseEntity.ok(Map.of(
                    "message", "Slide Up confirmed. Your seat is held and you remain eligible for higher preferences.",
                    "allotmentId", requestDTO.getAllotmentId()));

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Invalid request"));
        }
    }

    // =========================================================================
    // 4. SUBJECT PREFERENCES
    // =========================================================================
    @PostMapping("/save-combination-preferences")
    public ResponseEntity<Map<String, Object>> saveCombinationPreferences(
            @Valid @RequestBody SubjectPreferenceRequestDTO requestDTO, Authentication auth) {
        String applicantNo = auth.getName();
        try {
            counselingService.saveCombinationPreferences(applicantNo, requestDTO);

            return ResponseEntity.ok(Map.of("message", "Preferences saved successfully."));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Invalid request"));
        }
    }

    @GetMapping("/get-preferences/{allotmentId}")
    public ResponseEntity<SubjectPreferenceResponseDTO> getSavedPreferences(@PathVariable Long allotmentId,
                                                                            Authentication auth) {
        String applicantNo = auth.getName();
        return ResponseEntity.ok(counselingService.getSavedPreferences(applicantNo, allotmentId));
    }
}