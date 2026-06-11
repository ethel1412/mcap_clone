package nic.meg.mcap.controllers.pageControllers;

import nic.meg.mcap.entities.AdmissionWindow;
import nic.meg.mcap.repositories.AdmissionWindowRepository;
import nic.meg.mcap.services.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/schedule-management")
public class SchedulePageController {

    @Autowired
    private AdmissionWindowRepository admissionWindowRepository;

    @Autowired
    private ScheduleService scheduleService;

    @GetMapping("/list")
    public String listSchedules(Model model) {
        // Get all active admission windows
        List<AdmissionWindow> activeWindows = admissionWindowRepository
                .findByIsActive(true)
                .stream()
                .sorted((w1, w2) -> w2.getStartDate().compareTo(w1.getStartDate()))
                .collect(Collectors.toList());

        model.addAttribute("admissionWindows", activeWindows);
        return "schedule-management/list";
    }

    @GetMapping("/create")
    public String createSchedulePage(Model model) {
        // Get all active admission windows for dropdown
        List<AdmissionWindow> activeWindows = admissionWindowRepository
                .findByIsActive(true)
                .stream()
                .sorted((w1, w2) -> w2.getStartDate().compareTo(w1.getStartDate()))
                .collect(Collectors.toList());

        model.addAttribute("admissionWindows", activeWindows);
        return "schedule-management/create";
    }

    @GetMapping("/view")
    public String viewWindowSchedules(
            // CHANGED: Expect admissionCode instead of admissionId
            @RequestParam("admissionCode") String admissionCode, Model model) {

        AdmissionWindow window = admissionWindowRepository.findByAdmissionCode(admissionCode)
                .orElseThrow(() -> new RuntimeException("Admission window not found"));

        model.addAttribute("admissionWindow", window);
        return "schedule-management/view";
    }

    @GetMapping("/step-templates")
    public String manageStepTemplates() {
        return "schedule-management/step-templates";
    }
}