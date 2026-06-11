package nic.meg.mcap.controllers.pageControllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/eligibility")
public class EligibilityCriteriaPageController {

    @GetMapping("/configure")
    public String showConfigurationPage() {
        // This looks for 'configure-eligibility.html' in src/main/resources/templates/
        return "controller/eligibility/configure-eligibility";
    }
}