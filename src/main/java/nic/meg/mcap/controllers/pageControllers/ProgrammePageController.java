package nic.meg.mcap.controllers.pageControllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/programme-page")
public class ProgrammePageController {

    @GetMapping
    public String programmeHome(Model model) {
        // Add model attributes as needed for UI pages
        return "programmes/home";  // Template name for programme home page
    }

    // Additional UI endpoints can be added here
}
