package nic.meg.mcap.services;

import nic.meg.mcap.dto.request.AvailableSubjectsRequestDTO;
import nic.meg.mcap.dto.response.SubjectResponseDTO;
import nic.meg.mcap.enums.Shift;
import nic.meg.mcap.enums.SubjectType;
import java.util.List;
import java.util.Map;

public interface AvailableSubjectService {

    void saveAvailableSubjects(AvailableSubjectsRequestDTO requestDTO);

    Map<Shift, Map<SubjectType, List<SubjectResponseDTO>>> getAvailableSubjectsGroupedByShift(Integer programmeOfferedId);

    Map<SubjectType, List<SubjectResponseDTO>> getAvailableSubjectsForShift(Integer programmeOfferedId, Shift shift);
}