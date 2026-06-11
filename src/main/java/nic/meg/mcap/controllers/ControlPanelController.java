package nic.meg.mcap.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import nic.meg.mcap.services.AdmissionWindowService;
import nic.meg.mcap.services.InstituteService;
import nic.meg.mcap.services.ProgrammeService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@RequestMapping("/control-panel")
@PreAuthorize("hasRole('CONTROLLER')")
public class ControlPanelController {

    @Autowired
    private InstituteService instituteService;

    @Autowired
    private ProgrammeService programmeService;

    @Autowired
    private AdmissionWindowService admissionWindowService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
         // Add the count attributes your template expects
         model.addAttribute("totalInstitutesCount", instituteService.getTotalInstituteCount());
         model.addAttribute("acceptedInstitutesCount", instituteService.getInstituteCountByStatus("ACCEPTED"));
         model.addAttribute("rejectedInstitutesCount", instituteService.getInstituteCountByStatus("REJECTED"));
         model.addAttribute("pendingInstitutesCount", instituteService.getInstituteCountByStatus("PENDING"));
         model.addAttribute("correctionRequiredInstitutesCount", instituteService.getInstituteCountByStatus("CORRECTION_REQUIRED"));
 
         // Your existing attributes
         model.addAttribute("latestInstitutes", instituteService.getLatestInstitutes());
         model.addAttribute("institutes", instituteService.getAllInstitutes());
         model.addAttribute("allWindows", admissionWindowService.getAllAdmissionWindowsWithProgrammes());
         model.addAttribute("latestWindows", admissionWindowService.getLatestAdmissionWindows());
         model.addAttribute("activePage", "dashboard");
        return "control-panel/dashboard";
    }
}
