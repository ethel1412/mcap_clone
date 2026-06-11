package nic.meg.mcap.services.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import nic.meg.mcap.dto.request.SubjectAssignmentRequestDTO;
import nic.meg.mcap.dto.response.SubjectAssignmentResponseDTO;
import nic.meg.mcap.entities.Semester;
import nic.meg.mcap.entities.Subject;
import nic.meg.mcap.entities.SubjectAssignment;
import nic.meg.mcap.repositories.SemesterRepository;
import nic.meg.mcap.repositories.SubjectAssignmentRepository;
import nic.meg.mcap.repositories.SubjectRepository;
import nic.meg.mcap.services.SubjectAssignmentService;

@Service
public class SubjectAssignmentServiceImpl implements SubjectAssignmentService {

    @Autowired
    private SubjectAssignmentRepository subjectAssignmentRepository;

    @Autowired
    private SemesterRepository semesterRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Override
    @Transactional
    public List<SubjectAssignmentResponseDTO> assignSubjects(SubjectAssignmentRequestDTO requestDTO) {
        Semester semester = semesterRepository.findById(requestDTO.getSemesterId())
                .orElseThrow(() -> new EntityNotFoundException("Semester not found with ID: " + requestDTO.getSemesterId()));

        List<SubjectAssignment> newAssignments = new ArrayList<>();

        for (Integer subjectId : requestDTO.getSubjectIds()) {
            Subject subject = subjectRepository.findById(subjectId)
                    .orElseThrow(() -> new EntityNotFoundException("Subject not found with ID: " + subjectId));

            // Check if subject is already assigned to this semester
            if (subjectAssignmentRepository.existsBySemesterSemesterIdAndSubjectSubjectId(
                    requestDTO.getSemesterId(), subjectId)) {
                throw new IllegalStateException("Subject '" + subject.getSubjectName() +
                        "' is already assigned to this semester");
            }

            SubjectAssignment assignment = new SubjectAssignment();
            assignment.setSemester(semester);
            assignment.setSubject(subject);
            assignment.setActive(true);

            newAssignments.add(assignment);
        }

        List<SubjectAssignment> savedAssignments = subjectAssignmentRepository.saveAll(newAssignments);

        return savedAssignments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SubjectAssignmentResponseDTO> getSubjectsBySemester(Long semesterId) {
        List<SubjectAssignment> assignments = subjectAssignmentRepository.findBySemesterSemesterIdAndActiveTrue(semesterId);

        return assignments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void removeSubjectFromSemester(Long assignmentId) {
        if (!subjectAssignmentRepository.existsById(assignmentId)) {
            throw new EntityNotFoundException("Subject assignment not found with ID: " + assignmentId);
        }

        subjectAssignmentRepository.deleteById(assignmentId);
    }

    @Override
    public List<SubjectAssignmentResponseDTO> getSubjectsByProgramme(Integer ProgrammeOfferedId) {
        List<SubjectAssignment> assignments = subjectAssignmentRepository
                .findBySemesterProgrammeOfferedProgrammeOfferedIdAndActiveTrue(ProgrammeOfferedId);

        return assignments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private SubjectAssignmentResponseDTO convertToDTO(SubjectAssignment entity) {
        SubjectAssignmentResponseDTO dto = new SubjectAssignmentResponseDTO();
        dto.setAssignmentId(entity.getAssignmentId());
        dto.setSemesterId(entity.getSemester().getSemesterId());
        dto.setSemesterNumber(entity.getSemester().getSemesterNumber());
        dto.setSemesterName(entity.getSemester().getSemesterName());
        dto.setSubjectId(entity.getSubject().getSubjectId());
        dto.setSubjectName(entity.getSubject().getSubjectName());
        dto.setSubjectCode(entity.getSubject().getSubjectCode());
        dto.setActive(entity.isActive());
        return dto;
    }
}
