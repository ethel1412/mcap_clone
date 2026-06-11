package nic.meg.mcap.services;

import nic.meg.mcap.dto.request.EligibilityCriteriaRequestDTO;
import nic.meg.mcap.dto.response.EligibilityCriteriaResponseDTO;
import nic.meg.mcap.enums.ProgrammeLevel;

public interface EligibilityCriteriaService {
    
    EligibilityCriteriaResponseDTO saveCriteria(EligibilityCriteriaRequestDTO requestDTO);

    EligibilityCriteriaResponseDTO getCriteriaByWindowAndProgramme(String admissionCode, Short programmeId);
}