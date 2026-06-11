package nic.meg.mcap.controllers;

import java.security.Principal;
import java.util.List;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import nic.meg.mcap.dto.request.SubjectAssignmentRequestDTO;
import nic.meg.mcap.dto.response.SubjectAssignmentResponseDTO;
import nic.meg.mcap.services.SubjectAssignmentService;
import nic.meg.mcap.services.InstituteService;

@RestController
@RequestMapping("/subject-assignments/data")
//@PreAuthorize("hasAnyRole('ADMIN','INSTITUTE')")
public class SubjectAssignmentDataController {

    @Autowired
    private SubjectAssignmentService subjectAssignmentService;

    @Autowired
    private InstituteService instituteService;

    @PostMapping
    public ResponseEntity<List<SubjectAssignmentResponseDTO>> assignSubjects(@Valid @RequestBody SubjectAssignmentRequestDTO requestDTO) {
        List<SubjectAssignmentResponseDTO> assignments = subjectAssignmentService.assignSubjects(requestDTO);
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/by-semester/{semesterId}")
    public ResponseEntity<List<SubjectAssignmentResponseDTO>> getSubjectsBySemester(@PathVariable Long semesterId) {
        List<SubjectAssignmentResponseDTO> subjects = subjectAssignmentService.getSubjectsBySemester(semesterId);
        return ResponseEntity.ok(subjects);
    }

    @GetMapping("/by-programme/{programmeOfferedId}")
    public ResponseEntity<List<SubjectAssignmentResponseDTO>> getSubjectsByProgramme(@PathVariable Integer programmeOfferedId) {
        List<SubjectAssignmentResponseDTO> subjects = subjectAssignmentService.getSubjectsByProgramme(programmeOfferedId);
        return ResponseEntity.ok(subjects);
    }

    @DeleteMapping("/{assignmentId}")
    public ResponseEntity<Void> removeSubjectFromSemester(@PathVariable Long assignmentId) {
        subjectAssignmentService.removeSubjectFromSemester(assignmentId);
        return ResponseEntity.noContent().build();
    }
}
