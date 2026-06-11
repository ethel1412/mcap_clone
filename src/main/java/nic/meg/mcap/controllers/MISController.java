package nic.meg.mcap.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import nic.meg.mcap.repositories.InstituteDepartmentRepository;
import nic.meg.mcap.repositories.InstituteRepository;
import nic.meg.mcap.repositories.ProgrammesOfferedRepository;

@PreAuthorize("hasAnyRole('ADMIN','CONTROLLER')")
@Controller
@RequestMapping("/mis")
public class MISController {

	private final InstituteDepartmentRepository deptRepo;
	private final ProgrammesOfferedRepository progRepo;
	private final InstituteRepository instituteRepo;

	public MISController(InstituteDepartmentRepository deptRepo, ProgrammesOfferedRepository progRepo,
			InstituteRepository instituteRepo) {
		this.deptRepo = deptRepo;
		this.progRepo = progRepo;
		this.instituteRepo = instituteRepo;
	}

	// ✅ Page endpoint
	@GetMapping("/institute-mis")
	public String loadMISPage(Model model) {
		var data = instituteRepo.getMIS();
		model.addAttribute("list", data);
		return "controller/mis/institute-mis";
	}

	@GetMapping("/{id}/departments")
	@ResponseBody
	public List<String> getDepartments(@PathVariable Short id) {

		List<String> result = deptRepo.findByInstitute_InstituteId(id).stream()
				.map(d -> d.getDepartment().getDepartmentName()).distinct().collect(Collectors.toList());
		return result;
	}

	@GetMapping("/{id}/programmes")
	@ResponseBody
	public List<String> getProgrammes(@PathVariable Short id) {

		List<String> result = progRepo.findByInstituteDepartment_Institute_InstituteId(id).stream()
				.map(p -> p.getProgramme().getProgrammeName()).distinct().collect(Collectors.toList());
		return result;
	}
	
	@GetMapping("/{id}/programmes-with-shift")
	@ResponseBody
	public List<String> getProgrammesWithShift(@PathVariable Short id) {

	    return progRepo.findByInstituteDepartment_Institute_InstituteId(id)
	            .stream()
	            .collect(Collectors.groupingBy(
	                    p -> p.getProgramme().getProgrammeName(),
	                    Collectors.mapping(
	                            p -> p.getShift().getDisplayName(),
	                            Collectors.toSet()
	                    )
	            ))
	            .entrySet()
	            .stream()
	            .map(e -> e.getKey() + " (" + String.join(", ", e.getValue()) + ")")
	            .collect(Collectors.toList());
	}
}