package nic.meg.mcap.services;

import nic.meg.mcap.dto.request.ProgrammePreferenceRequestDTO;
import nic.meg.mcap.dto.response.PreferenceApplicantDTO;
import nic.meg.mcap.dto.response.ProgrammePreferenceResponseDTO; // Import new DTO
import nic.meg.mcap.entities.ApplicantProgrammePreference;
import nic.meg.mcap.enums.ApplicantType;

import java.util.List;

public interface ProgrammePreferenceService {
    // Added applicantNo for security, changed return type
    List<ProgrammePreferenceResponseDTO> savePreferences(ProgrammePreferenceRequestDTO requestDTO, String applicantNo);

    // Added applicantNo for security, changed return type
    List<ProgrammePreferenceResponseDTO> getPreferencesByApplicationId(Long applicationId, String applicantNo);

    void deletePreferencesByApplicationId(Long applicationId);

    boolean hasPreferences(Long applicationId, String applicantNo);

    List<PreferenceApplicantDTO> getApplicantsWhoPreferredProgramme(String admissionWindowCode, Short programmeId);

    List<PreferenceApplicantDTO> getApplicantsForProgramme(
            String admissionWindowCode, int programmeId, ApplicantType applicantType);

}