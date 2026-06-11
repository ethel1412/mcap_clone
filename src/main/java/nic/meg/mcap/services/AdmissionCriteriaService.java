package nic.meg.mcap.services;

import nic.meg.mcap.dto.request.AdmissionCriteriaRequestDTO;
import nic.meg.mcap.dto.response.AdmissionCriteriaResponseDTO;
import nic.meg.mcap.dto.response.ProgrammeWithCriteriaDTO;

import java.util.List;

public interface AdmissionCriteriaService {

    AdmissionCriteriaResponseDTO saveOrUpdateCriteria(AdmissionCriteriaRequestDTO requestDTO);

    // UG is programme-wise now
    AdmissionCriteriaResponseDTO getCriteriaForUG(Short admissionWindowId, Short programmeId);

    // PG criteria by programmeId
    AdmissionCriteriaResponseDTO getCriteriaForPG(Short admissionWindowId, Short programmeId);

    boolean hasCriteria(Short admissionWindowId, Short streamId, Short programmeId);

    void deleteCriteria(Long criteriaId);

    // Existing list helper (by stream+level from window)
    List<ProgrammeWithCriteriaDTO> getProgrammesWithCriteriaStatus(Short admissionWindowId);

    // Preferred list helper (from ProgrammeOffered table)
    List<ProgrammeWithCriteriaDTO> getProgrammeOfferedWithCriteriaStatus(Short admissionWindowId);
}
