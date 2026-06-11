package nic.meg.mcap.controllers.pageControllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/controller/approvals")
// Ensure only Controller can access this page
@PreAuthorize("hasRole('CONTROLLER')")
public class ProgrammeApprovalPageController {

    @GetMapping("/programmes")
    public String showProgrammeApprovalPage() {
        // This maps to templates/controller/programme-approvals.html
        return "controller/programme-approvals";
    }
}