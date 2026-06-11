package nic.meg.mcap.services;

import nic.meg.mcap.dto.request.SubjectAssignmentRequestDTO;
import nic.meg.mcap.dto.response.SubjectAssignmentResponseDTO;

import java.util.List;

public interface SubjectAssignmentService {

    List<SubjectAssignmentResponseDTO> assignSubjects(SubjectAssignmentRequestDTO requestDTO);

    List<SubjectAssignmentResponseDTO> getSubjectsBySemester(Long semesterId);

    void removeSubjectFromSemester(Long assignmentId);

    List<SubjectAssignmentResponseDTO> getSubjectsByProgramme(Integer ProgrammeOfferedId);
}
