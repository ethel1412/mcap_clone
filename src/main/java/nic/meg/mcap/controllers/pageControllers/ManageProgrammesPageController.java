package nic.meg.mcap.controllers.pageControllers;

import lombok.RequiredArgsConstructor;
import nic.meg.mcap.dto.response.SeatMatrixResponseDTO;
import nic.meg.mcap.entities.AdmissionWindow;
import nic.meg.mcap.entities.ProgrammeOffered;
import nic.meg.mcap.services.AdmissionWindowService;
import nic.meg.mcap.services.ProgrammeOfferedService;
import nic.meg.mcap.services.SeatMatrixService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admission-window/institute")
@RequiredArgsConstructor
public class ManageProgrammesPageController {

    private final AdmissionWindowService admissionWindowService;
    private final ProgrammeOfferedService programmeOfferedService;
    private final SeatMatrixService seatMatrixService;

    @GetMapping
    public String showAdmissionWindowPage(Model model) {
        List<AdmissionWindow> windows = admissionWindowService.getLatestAdmissionWindows();
        model.addAttribute("windows", windows);
        return "manage-programmes/admission-window";
    }

    // CHANGED: {admissionId} to {admissionCode}
    @GetMapping("/{admissionCode}/programmes")
    public String showWindowProgrammesPage(
            // CHANGED: Short admissionId to String admissionCode
            @PathVariable("admissionCode") String admissionCode,
            Model model) {

        // CHANGED: Fetch the window using the business code
        AdmissionWindow window = admissionWindowService.findByCode(admissionCode);

        if (window == null) {
            throw new RuntimeException("Admission window not found with Code: " + admissionCode);
        }

        // EXTRACT the internal ID to use with the existing service methods
        Short admissionId = window.getAdmissionId();

        // Get all programmes offered in this window
        List<ProgrammeOffered> programmes = programmeOfferedService
                .getProgrammesForAdmissionWindow(admissionId);

        // Get seat matrices for all programmes
        List<SeatMatrixResponseDTO> seatMatrices = seatMatrixService
                .getByAdmissionWindow(admissionId);

        // Convert to map for easy lookup in Thymeleaf
        Map<Integer, SeatMatrixResponseDTO> seatMatrixMap = seatMatrices.stream()
                .collect(Collectors.toMap(SeatMatrixResponseDTO::getProgrammeOfferedId, sm -> sm));

        // Add to model
        model.addAttribute("window", window);
        model.addAttribute("programmes", programmes);
        model.addAttribute("seatMatrixMap", seatMatrixMap);

        // CHANGED: Pass the code to Thymeleaf instead of the internal ID
        model.addAttribute("admissionCode", admissionCode);

        return "manage-programmes/admission-window-programmes";
    }

    // CHANGED: Fixed the redundant path mapping and updated to {admissionCode}
    @GetMapping("/{admissionCode}/programme/{poId}")
    public String showProgrammePage(
            // CHANGED: Short admissionId to String admissionCode
            @PathVariable("admissionCode") String admissionCode,
            @PathVariable("poId") Integer poId,
            Model model) {

        // CHANGED: Fetch by code
        var window = admissionWindowService.findByCode(admissionCode);

        // Assuming this returns Optional<ProgrammeOffered>
        var poOpt = programmeOfferedService.findById(poId);
        if (poOpt.isEmpty()) {
            throw new IllegalArgumentException("ProgrammeOffered not found for id=" + poId);
        }
        var po = poOpt.get();

        model.addAttribute("window", window);
        model.addAttribute("programmeOffered", po);

        // CHANGED: Pass the code to Thymeleaf instead of the internal ID
        model.addAttribute("admissionCode", admissionCode);
        model.addAttribute("programmeOfferedId", poId);

        // If you want level on the page, you can still use:
        // model.addAttribute("programmeLevel", po.getProgramme().getProgrammeLevel().name());

        return "manage-programmes/admission-criteria-details";
    }
}