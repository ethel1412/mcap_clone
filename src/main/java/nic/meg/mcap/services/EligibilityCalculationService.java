package nic.meg.mcap.services;

import nic.meg.mcap.dto.response.EligibilityListRowDTO;
import nic.meg.mcap.dto.response.EligibilityResultResponseDTO;
import nic.meg.mcap.entities.Application;
import nic.meg.mcap.enums.ApplicantType;

import java.util.List;

public interface EligibilityCalculationService {

    /**
     * Calculates eligibility for all programme preferences in the given application.
     * Compares Class XII marks against the defined Eligibility Criteria.
     * Saves the result (Eligible/Not Eligible) into the EligibilityResult table.
     *
     * @param application The verified application to process.
     */
    void calculateAndSaveEligibility(Application application);


    // CHANGED: short admissionWindowId to String admissionCode
    List<EligibilityResultResponseDTO> getEligibilityForProgramme(
            String admissionCode, int programmeId, ApplicantType applicantType);

    // CHANGED: short admissionWindowId to String admissionCode
    List<EligibilityListRowDTO> getEligibilityListRowsForProgramme(
            String admissionCode, int programmeId, ApplicantType applicantType);

}