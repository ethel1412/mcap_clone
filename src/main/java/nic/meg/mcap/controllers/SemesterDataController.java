package nic.meg.mcap.controllers;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import nic.meg.mcap.dto.request.SemesterRequestDTO;
import nic.meg.mcap.dto.response.SemesterResponseDTO;
import nic.meg.mcap.dto.response.SubjectResponseDTO;
import nic.meg.mcap.entities.Subject;
import nic.meg.mcap.services.SemesterService;
import nic.meg.mcap.services.SubjectService;

@RestController
@RequestMapping("/semesters/data")
@PreAuthorize("hasAnyRole('ADMIN','INSTITUTE')")
public class SemesterDataController {

    @Autowired
    private SemesterService semesterService;

    @Autowired
    private SubjectService subjectService;

    // Get semesters by programme
    @GetMapping("/by-programme/{programmeOfferedId}")
    public ResponseEntity<List<SemesterResponseDTO>> getSemestersByProgramme(@PathVariable Integer programmeOfferedId) {
        List<SemesterResponseDTO> semesters = semesterService.getSemestersByProgramme(programmeOfferedId);
        return ResponseEntity.ok(semesters);
    }

    // Create new semester
    @PostMapping
    public ResponseEntity<SemesterResponseDTO> createSemester(@Valid @RequestBody SemesterRequestDTO request) {
        SemesterResponseDTO semester = semesterService.createSemester(request);
        return ResponseEntity.ok(semester);
    }

    // Delete semester (Using Long as per your SemesterResponseDTO)
    @DeleteMapping("/{semesterId}")
    public ResponseEntity<Void> deleteSemester(@PathVariable Long semesterId) {
        semesterService.deleteSemester(semesterId);
        return ResponseEntity.ok().build();
    }

    // NEW METHOD: Get all subjects with optional search for INSTITUTE users
    @GetMapping("/all-subjects")
    public ResponseEntity<List<SubjectResponseDTO>> getAllSubjectsForInstitute(
            @RequestParam(value = "search", required = false) String search) {

        List<Subject> subjects;

        if (search != null && !search.trim().isEmpty()) {
            // Use the existing searchSubjectsByName method
            subjects = subjectService.searchSubjectsByName(search.trim());
        } else {
            // Get all subjects
            subjects = subjectService.getAllSubjects();
        }

        List<SubjectResponseDTO> dtos = subjects.stream()
                .map(this::convertSubjectToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // Helper method to convert Subject entity to DTO
    private SubjectResponseDTO convertSubjectToDTO(Subject entity) {
        SubjectResponseDTO dto = new SubjectResponseDTO();
        dto.setSubjectId(entity.getSubjectId());
        dto.setSubjectName(entity.getSubjectName());
        dto.setSubjectCode(entity.getSubjectCode());
        return dto;
    }
}
