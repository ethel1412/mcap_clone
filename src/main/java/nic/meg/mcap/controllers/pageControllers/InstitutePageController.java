package nic.meg.mcap.controllers.pageControllers;

import lombok.RequiredArgsConstructor;
import nic.meg.mcap.dto.request.AddressDTO;
import nic.meg.mcap.dto.request.InstituteRequestDTO;
import nic.meg.mcap.dto.response.InstituteAllotmentDTO;
import nic.meg.mcap.entities.Institute;
import nic.meg.mcap.enums.AllotmentStatus;
import nic.meg.mcap.repositories.SeatAllotmentRepository;
import nic.meg.mcap.services.AffiliationTypeService;
import nic.meg.mcap.services.CounselingService;
import nic.meg.mcap.services.InstituteService;
import nic.meg.mcap.services.ManagementTypeService;
import nic.meg.mcap.services.MasterService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/institute")
@RequiredArgsConstructor
public class InstitutePageController {

    private final CounselingService counselingService;
    private final InstituteService instituteService;
    private final MasterService masterService;
    private final AffiliationTypeService affiliationTypeService;
    private final ManagementTypeService managementTypeService;

    // NEW: Injected to fetch distinct programmes for the filter dropdown
    private final SeatAllotmentRepository seatAllotmentRepository;

    @GetMapping("/verification-dashboard")
    public String showVerificationDashboard(Model model, Authentication authentication) {
        String username = authentication.getName();
        Short instituteId = instituteService.getInstituteIdByUsername(username);
        String instituteName = instituteService.findById(instituteId).getInstituteName();

        // 1. Pending Tab: Only PENDING_VERIFICATION
        List<InstituteAllotmentDTO> pendingAllotments =
                counselingService.getPendingVerificationAllotmentsForInstitute(instituteId);

        // 2. Verified Tab: PENDING (Institute Verified) + ACCEPTED (Applicant Accepted)
        List<InstituteAllotmentDTO> verifiedAllotments =
                counselingService.getAllotmentsByStatusList(instituteId,
                        Arrays.asList(AllotmentStatus.PENDING, AllotmentStatus.ACCEPTED));

        // 3. Rejected Tab: INSTITUTE_REJECTED + REJECTED (Applicant Rejected)
        List<InstituteAllotmentDTO> rejectedAllotments =
                counselingService.getAllotmentsByStatusList(instituteId,
                        Arrays.asList(AllotmentStatus.INSTITUTE_REJECTED, AllotmentStatus.REJECTED));

        model.addAttribute("instituteName", instituteName);
        model.addAttribute("pendingAllotments", pendingAllotments);
        model.addAttribute("verifiedAllotments", verifiedAllotments);
        model.addAttribute("rejectedAllotments", rejectedAllotments);

        // NEW: Fetch distinct programmes available for this specific institute and pass to the UI
        model.addAttribute("instituteProgrammes", seatAllotmentRepository.findDistinctProgrammesByInstituteId(instituteId));

        return "institute/verification-dashboard";
    }

    @GetMapping("/institute-dashboard")
    public String showDashboardPage() {
        return "institute/institute-dashboard";
    }

    @GetMapping("/profile")
    public String editInstituteProfile(Principal principal, Model model) {

        Short instituteId = instituteService.getInstituteIdByUsername(principal.getName());
        Institute inst = instituteService.findById(instituteId);
        InstituteRequestDTO dto = instituteService.convertToRequestDTO(inst);

        if (dto.getAddressDTO() == null) {
            dto.setAddressDTO(new AddressDTO());
        }

        model.addAttribute("inst", dto);

        model.addAttribute("affiliationTypes", affiliationTypeService.getAll());
        model.addAttribute("managementTypes", managementTypeService.getAll());

        // States always loaded
        model.addAttribute("states", masterService.getListStates());

        // Load districts if state already selected
        if (dto.getAddressDTO().getStateCode() != null) {
            model.addAttribute("districts",
                    masterService.getListOfDistrict((dto.getAddressDTO().getStateCode())));
        }

        // Load blocks if district already selected
        if (dto.getAddressDTO().getDistrictCode() != null) {
            model.addAttribute("blocks",
                    masterService.getListOfBlocks((dto.getAddressDTO().getDistrictCode())));
        }

        return "institute/institute-profile";
    }
}