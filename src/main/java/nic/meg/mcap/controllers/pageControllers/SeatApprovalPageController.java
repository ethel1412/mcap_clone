package nic.meg.mcap.controllers.pageControllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/controller/seat-approvals")
@PreAuthorize("hasRole('CONTROLLER')")
public class SeatApprovalPageController {

    @GetMapping
    public String showPage() {
        return "controller/seat-approvals";
    }
}