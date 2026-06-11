package nic.meg.mcap.controllers.pageControllers;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import nic.meg.mcap.dto.request.InstituteDepartmentBatchAssignRequestDTO;
import nic.meg.mcap.dto.request.InstituteDepartmentRequestDTO;
import nic.meg.mcap.services.DepartmentService;
import nic.meg.mcap.services.InstituteDepartmentService;
import nic.meg.mcap.services.InstituteService;

@Controller
@RequestMapping("/institute-departments/page")
public class InstituteDepartmentPageController {

	@Autowired
	private InstituteDepartmentService instituteDepartmentService;

	@Autowired
	private DepartmentService departmentService;

	@Autowired
	private InstituteService instituteService;

	@GetMapping("/assign")
	public String showAssignPage(Model model, Principal principal) {
		model.addAttribute("departments", departmentService.getAllDepartments());

		InstituteDepartmentRequestDTO dto = new InstituteDepartmentRequestDTO();
		Short instituteId = instituteService.findInstituteIdByUsername(principal.getName());
		dto.setInstituteId(instituteId);

		model.addAttribute("instituteDepartmentRequestDTO", dto);

		return "institute-departments/assign";
	}

	@PostMapping("/assign")
	public String assignDepartment(
			@Valid @ModelAttribute("instituteDepartmentRequestDTO") InstituteDepartmentRequestDTO dto,
			BindingResult bindingResult, Model model, Principal principal) {

		if (bindingResult.hasErrors()) {
			model.addAttribute("departments", departmentService.getAllDepartments());
			return "institute-departments/assign";
		}

		Short loggedInInstituteId = instituteService.findInstituteIdByUsername(principal.getName());

		try {
			instituteDepartmentService.assignDepartmentsToInstitute(dto, loggedInInstituteId);
			model.addAttribute("successMessage", "Department assigned successfully.");

		} catch (EntityNotFoundException e) {
			model.addAttribute("errorMessage", e.getMessage());
		}

		model.addAttribute("departments", departmentService.getAllDepartments());
		return "institute-departments/assign";
	}

	@PostMapping(value = "/assign-multiple", consumes = "application/json")
	public ResponseEntity<?> assignDepartmentsViaAjax(
			@RequestBody @Valid InstituteDepartmentBatchAssignRequestDTO batchAssignDTO, Principal principal) {
		try {
			Short loggedInInstituteId = instituteService.findInstituteIdByUsername(principal.getName());

			instituteDepartmentService.assignDepartmentsToInstitute(batchAssignDTO, loggedInInstituteId);

			return ResponseEntity.ok().build();

		} catch (EntityNotFoundException ex) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());

		} catch (IllegalArgumentException ex) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
		}
	}

	@GetMapping("/my")
	public String showMyDepartmentsPage() {
		return "institute-departments/my-departments";
	}
}