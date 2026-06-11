package nic.meg.mcap.controllers;

import lombok.RequiredArgsConstructor;
import nic.meg.mcap.dto.response.ScheduleStepTemplateDTO;
import nic.meg.mcap.services.ScheduleStepTemplateService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/roadmap")
@RequiredArgsConstructor
public class RoadmapReadController {

    private final ScheduleStepTemplateService templateService;

    @GetMapping
    public List<ScheduleStepTemplateDTO> getRoadmap() {
        return templateService.getAllActiveTemplates();
    }
}
