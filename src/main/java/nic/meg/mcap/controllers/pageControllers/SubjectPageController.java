package nic.meg.mcap.controllers.pageControllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/subjects/page")
public class SubjectPageController {

    @GetMapping
    public String getSubjectsPage(Model model) {
        return "subjects/subjects";
    }

    // Additional UI endpoints can be added here
}