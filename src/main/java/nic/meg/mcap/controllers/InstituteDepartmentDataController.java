package nic.meg.mcap.controllers;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import nic.meg.mcap.dto.request.InstituteDepartmentBatchAssignRequestDTO;
import nic.meg.mcap.dto.request.InstituteDepartmentRequestDTO;
import nic.meg.mcap.dto.response.DepartmentResponseDTO;
import nic.meg.mcap.dto.response.InstituteDepartmentResponseDTO;
import nic.meg.mcap.services.DepartmentService;
import nic.meg.mcap.services.InstituteDepartmentService;
import nic.meg.mcap.services.InstituteService;

@RestController
@RequestMapping("/institute-departments/data")
@Validated
public class InstituteDepartmentDataController {

	@Autowired
	private InstituteDepartmentService instituteDepartmentService;

	@Autowired
	private DepartmentService departmentService;

	@Autowired
	private InstituteService instituteService;

	@GetMapping
	public ResponseEntity<List<InstituteDepartmentResponseDTO>> getAll() {
		List<InstituteDepartmentResponseDTO> list = instituteDepartmentService.getAllInstituteDepartments();
		return ResponseEntity.ok(list);
	}

	@GetMapping(path = "/departments")
	public ResponseEntity<List<DepartmentResponseDTO>> getAllDepartments() {
		List<DepartmentResponseDTO> departments = departmentService.getAllDepartments();
		return ResponseEntity.ok(departments);
	}

	@GetMapping("/{id}")
	public ResponseEntity<InstituteDepartmentResponseDTO> getById(@PathVariable Integer id) {
		InstituteDepartmentResponseDTO dto = instituteDepartmentService.getInstituteDepartmentById(id);
		return ResponseEntity.ok(dto);
	}

	@PostMapping
	public ResponseEntity<?> create(@RequestBody InstituteDepartmentRequestDTO requestDTO, Principal principal) {

		try {
			Short instituteId = instituteService.findInstituteIdByUsername(principal.getName());
			requestDTO.setInstituteId(instituteId);

			Set<ConstraintViolation<InstituteDepartmentRequestDTO>> violations = Validation
					.buildDefaultValidatorFactory().getValidator().validate(requestDTO);

			if (!violations.isEmpty()) {
				Map<String, String> errors = new HashMap<>();
				for (ConstraintViolation<InstituteDepartmentRequestDTO> violation : violations) {
					errors.put(violation.getPropertyPath().toString(), violation.getMessage());
				}
				return ResponseEntity.badRequest().body(Map.of("message", "Validation failed", "errors", errors));
			}

			InstituteDepartmentResponseDTO created = instituteDepartmentService.createInstituteDepartment(requestDTO,
					instituteId);
			return ResponseEntity.ok(created);
		} catch (SecurityException e) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
		}
	}

	@PutMapping("/{instituteDepartmentId}")
	public ResponseEntity<?> updateDepartment(@PathVariable Integer instituteDepartmentId,
			@RequestBody Map<String, Object> updateData, Principal principal) {
		try {
			Short loggedInInstituteId = instituteService.findInstituteIdByUsername(principal.getName());

			InstituteDepartmentRequestDTO updateDTO = new InstituteDepartmentRequestDTO();
			updateDTO.setHodName((String) updateData.get("hodName"));
			updateDTO.setEmail((String) updateData.get("email"));
			updateDTO.setPhone((String) updateData.get("phone"));
			updateDTO.setActive((Boolean) updateData.get("active"));

			InstituteDepartmentResponseDTO updated = instituteDepartmentService
					.updateInstituteDepartment(instituteDepartmentId, updateDTO, loggedInInstituteId);

			return ResponseEntity.ok(updated);

		} catch (SecurityException e) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Forbidden")); // don’t echo
																										// internal
																										// details

		} catch (jakarta.persistence.EntityNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Department not found."));
		}
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<?> delete(@PathVariable Integer id, Principal principal) {
		try {
			Short loggedInInstituteId = instituteService.findInstituteIdByUsername(principal.getName());

			instituteDepartmentService.deleteInstituteDepartment(id, loggedInInstituteId);

			return ResponseEntity.ok(Map.of("message", "Department removed successfully"));

		} catch (SecurityException e) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Forbidden")); // don’t expose
																										// internals

		} catch (org.springframework.dao.DataIntegrityViolationException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message",
					"Cannot delete: This department has linked programmes. Please remove all linked programmes first."));
		}
	}

	@PostMapping("/assign-multiple")
	public ResponseEntity<?> assignMultipleDepartments(
			@Valid @RequestBody InstituteDepartmentBatchAssignRequestDTO batchRequest, Principal principal) {
		Short loggedInInstituteId = instituteService.findInstituteIdByUsername(principal.getName());
		instituteDepartmentService.assignDepartmentsToInstitute(batchRequest, loggedInInstituteId);
		return ResponseEntity.ok().build();

	}

	@GetMapping("/by-institute")
	public ResponseEntity<List<InstituteDepartmentResponseDTO>> getByInstitute(@RequestParam Short instituteId) {
		List<InstituteDepartmentResponseDTO> list = instituteDepartmentService.getByInstituteId(instituteId);
		return ResponseEntity.ok(list);
	}

	@GetMapping("/my")
	public ResponseEntity<List<InstituteDepartmentResponseDTO>> getByLoggedInInstitute(Principal principal) {
		String username = principal.getName();
		Short instituteId = instituteService.findInstituteIdByUsername(username);
		List<InstituteDepartmentResponseDTO> list = instituteDepartmentService.getByInstituteId(instituteId);
		return ResponseEntity.ok(list);
	}
}