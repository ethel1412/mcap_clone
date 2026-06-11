package nic.meg.mcap.controllers.pageControllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/programme-subjects")
public class ProgrammeSubjectsPageController {

    @GetMapping
    public String getProgrammeSubjectsPage(Model model,
                                        @RequestParam Integer programmeOfferedId,
                                        @RequestParam String programmeName,
                                        @RequestParam String departmentName) {

        model.addAttribute("programmeOfferedId", programmeOfferedId);
        model.addAttribute("programmeName", programmeName);
        model.addAttribute("departmentName", departmentName);

        return "institute-departments/programme-subjects";
    }
}
