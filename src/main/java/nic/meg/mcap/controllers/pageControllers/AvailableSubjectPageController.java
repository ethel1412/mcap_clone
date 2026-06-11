package nic.meg.mcap.controllers.pageControllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import nic.meg.mcap.entities.InstituteDepartment;
import nic.meg.mcap.entities.ProgrammeOffered;
import nic.meg.mcap.repositories.InstituteDepartmentRepository;
import nic.meg.mcap.repositories.ProgrammesOfferedRepository;

@Controller
@RequestMapping("/institute/programmes")
public class AvailableSubjectPageController {
	@Autowired ProgrammesOfferedRepository progOffRepo;
	
    @GetMapping("/combinations/manage")
    public String getAvailableSubjectsPage(Model model,
                                           @RequestParam Integer programmeOfferedId)
   {

    	ProgrammeOffered progOff = progOffRepo.findById(programmeOfferedId).orElseThrow();
    	model.addAttribute("programmeOfferedId", programmeOfferedId);
    	model.addAttribute("programmeName", progOff.getProgramme().getProgrammeName());
    	model.addAttribute("departmentName", progOff.getInstituteDepartment().getDepartment().getDepartmentName());
    	model.addAttribute("shiftName", progOff.getShift().getDisplayName());
    	return "institute-departments/manage-combinations";
    }
}