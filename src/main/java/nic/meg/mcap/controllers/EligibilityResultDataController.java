package nic.meg.mcap.controllers;

import lombok.RequiredArgsConstructor;
import nic.meg.mcap.dto.response.EligibilityResultResponseDTO;
import nic.meg.mcap.dto.response.EligibilityListRowDTO;
import nic.meg.mcap.enums.ApplicantType;
import nic.meg.mcap.services.EligibilityCalculationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/eligibility/data")
@RequiredArgsConstructor
public class EligibilityResultDataController {

    private final EligibilityCalculationService eligibilityCalculationService;

    // Existing endpoint – detailed per-programme eligibility result
    @GetMapping("/programme")
    public List<EligibilityResultResponseDTO> getProgrammeEligibility(
            @RequestParam("admissionWindowCode") String admissionWindowCode,
            @RequestParam Integer programmeId,
            @RequestParam ApplicantType applicantType) {
        return eligibilityCalculationService
                .getEligibilityForProgramme(admissionWindowCode, programmeId, applicantType);
    }

    // New endpoint – grid rows for eligibility list
    @GetMapping("/programme/list")
    public List<EligibilityListRowDTO> getProgrammeEligibilityList(
            @RequestParam("admissionWindowCode") String admissionWindowCode,
            @RequestParam Integer programmeId,
            @RequestParam ApplicantType applicantType) {

        return eligibilityCalculationService
                .getEligibilityListRowsForProgramme(admissionWindowCode, programmeId, applicantType);
    }
}