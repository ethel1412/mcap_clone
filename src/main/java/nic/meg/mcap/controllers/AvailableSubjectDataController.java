package nic.meg.mcap.controllers;

import jakarta.validation.Valid;
import nic.meg.mcap.dto.request.AvailableSubjectsRequestDTO;
import nic.meg.mcap.dto.response.SubjectResponseDTO;
import nic.meg.mcap.enums.Shift;
import nic.meg.mcap.enums.SubjectType;
import nic.meg.mcap.services.AvailableSubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/combinations")
public class AvailableSubjectDataController {

    @Autowired
    private AvailableSubjectService availableSubjectService;

    @PostMapping
    @PreAuthorize("hasRole('INSTITUTE')")
    public ResponseEntity<?> saveAvailableSubjectsForShift(@Valid @RequestBody AvailableSubjectsRequestDTO requestDTO) {
        availableSubjectService.saveAvailableSubjects(requestDTO);
        return ResponseEntity.ok(Map.of("message", "Available subjects saved successfully."));
    }

    @GetMapping("/by-programme-offered/{programmeOfferedId}")
    @PreAuthorize("hasRole('INSTITUTE')")
    public ResponseEntity<Map<Shift, Map<SubjectType, List<SubjectResponseDTO>>>> getAvailableSubjectsByProgrammeOffered(
            @PathVariable Integer programmeOfferedId) {
        Map<Shift, Map<SubjectType, List<SubjectResponseDTO>>> subjects = availableSubjectService.getAvailableSubjectsGroupedByShift(programmeOfferedId);
        return ResponseEntity.ok(subjects);
    }

    @GetMapping("/for-applicant/{programmeOfferedId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<SubjectType, List<SubjectResponseDTO>>> getCombinationsForApplicant(
            @PathVariable Integer programmeOfferedId,
            @RequestParam Shift shift) {
        Map<SubjectType, List<SubjectResponseDTO>> subjects = availableSubjectService.getAvailableSubjectsForShift(programmeOfferedId, shift);
        return ResponseEntity.ok(subjects);
    }
}