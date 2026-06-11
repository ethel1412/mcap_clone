package nic.meg.mcap.controllers.pageControllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/programmes-offered/page")
public class ProgrammeOfferedPageController {

    @GetMapping
    public String getProgrammesOfferedPage() {
        return "institute-departments/programmesOffered";
    }
}
