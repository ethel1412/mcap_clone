package nic.meg.mcap.services;

import nic.meg.mcap.dto.request.SemesterRequestDTO;
import nic.meg.mcap.dto.response.SemesterResponseDTO;
import nic.meg.mcap.dto.response.SubjectResponseDTO;

import java.util.List;

public interface SemesterService {

    List<SemesterResponseDTO> getSemestersByProgramme(Integer ProgrammeOfferedId);

    SemesterResponseDTO createSemester(SemesterRequestDTO requestDTO);

    SemesterResponseDTO updateSemester(Long semesterId, SemesterRequestDTO requestDTO);

    void deleteSemester(Long semesterId);

    List<SubjectResponseDTO> getAvailableSubjectsForSemester(Long semesterId);
}
