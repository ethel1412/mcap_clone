package nic.meg.mcap.controllers.pageControllers;

import nic.meg.mcap.dto.response.ActiveAdmissionWindowResponseDTO;
import nic.meg.mcap.dto.response.UpcomingAdmissionWindowResponseDTO;
import nic.meg.mcap.dto.response.ProgrammeWithCriteriaDTO;
import nic.meg.mcap.entities.AdmissionWindow;
import nic.meg.mcap.repositories.AdmissionWindowRepository;
import nic.meg.mcap.services.AdmissionCriteriaService;
import nic.meg.mcap.services.AdmissionWindowService;
import nic.meg.mcap.services.UpcomingAdmissionWindowQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/admission-criteria/page")
//@PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
public class AdmissionCriteriaPageController {

    @Autowired
    private AdmissionWindowService admissionWindowService;
    @Autowired
    private UpcomingAdmissionWindowQueryService upcomingWindowService;

    @Autowired
    private AdmissionWindowRepository admissionWindowRepository;

    @Autowired
    private AdmissionCriteriaService criteriaService;

    // Page 1: Select Admission Window
    @GetMapping("/select-window")
    public String selectWindow(Model model) {
        // Get all ACTIVE admission windows (CHANGED)
        List<ActiveAdmissionWindowResponseDTO> activeWindows =
                admissionWindowService.findActiveAdmissionWindows();

        model.addAttribute("upcomingWindows", activeWindows);  // Keep same name for frontend
        return "admission-criteria/select-window";
    }

    // Page 2: Setup Weightage for Selected Window
    @GetMapping("/setup")
    public String setupCriteria(
            @RequestParam Short admissionWindowId,
            Model model) {

        // Get admission window details
        AdmissionWindow window = admissionWindowRepository.findById(admissionWindowId)
                .orElseThrow(() -> new RuntimeException("Admission window not found"));

        // Get distinct programmes from ProgrammeOffered with their criteria status
        List<ProgrammeWithCriteriaDTO> programmes =
                criteriaService.getProgrammeOfferedWithCriteriaStatus(admissionWindowId);

        model.addAttribute("admissionWindow", window);
        model.addAttribute("programmes", programmes);
        model.addAttribute("streamName", window.getStream() != null ? window.getStream().getStreamName() : "All Streams");
        model.addAttribute("programmeLevel", window.getProgrammeLevel());
        model.addAttribute("admissionSession", window.getSession());

        return "admission-criteria/setup-criteria";
    }

}