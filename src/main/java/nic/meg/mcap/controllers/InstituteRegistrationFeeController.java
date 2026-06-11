package nic.meg.mcap.controllers;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import nic.meg.mcap.dto.request.InstituteRegistrationFeeRequestDTO;
import nic.meg.mcap.dto.response.InstituteRegistrationFeeResponseDTO;
import nic.meg.mcap.entities.User;
import nic.meg.mcap.enums.Caste;
import nic.meg.mcap.repositories.UserRepository;
import nic.meg.mcap.services.InstituteRegistrationFeeService;

@Controller
@RequestMapping("/institute-registration-fee")
@PreAuthorize("hasRole('INSTITUTE')")
public class InstituteRegistrationFeeController {

	@Autowired
	private InstituteRegistrationFeeService feeService;

	@Autowired
	private UserRepository userRepository;

	// Display the registration fee management page
	@GetMapping
	public String showRegistrationFeePage(Principal principal, Model model, HttpServletRequest request) {
		try {
			User user = userRepository.findByUsername(principal.getName())
					.orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("User not found"));

			List<InstituteRegistrationFeeResponseDTO> fees = feeService.getFeesByUserId(user.getUserId());

			model.addAttribute("fees", fees);
			model.addAttribute("casteOptions", Caste.values());

			CsrfToken csrf = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
			if (csrf != null) {
				model.addAttribute("_csrf", csrf);
				model.addAttribute("_csrf_header", csrf.getHeaderName());
			}

			return "institute-registration-fee";

		} catch (jakarta.persistence.EntityNotFoundException e) {
			model.addAttribute("error", "User not found.");
			return "error";

		} catch (org.springframework.dao.DataAccessException e) {
			model.addAttribute("error", "Unable to load data. Please try again.");
			return "error";
		}
	}

	// Save or update registration fee
	@PostMapping("/save")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> saveFee(Principal principal,
			@Valid @RequestBody InstituteRegistrationFeeRequestDTO feeDTO, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			String errorMessage = getValidationErrorMessage(bindingResult);
			return ResponseEntity.badRequest().body(createErrorResponse("Validation error: " + errorMessage));
		}

		User user = userRepository.findByUsername(principal.getName())
				.orElseThrow(() -> new RuntimeException("User not found"));

		InstituteRegistrationFeeResponseDTO savedFee = feeService.saveFee(user.getUserId(), feeDTO);

		return ResponseEntity.ok(createSuccessResponse("Registration fee saved successfully", savedFee));
	}

	// Get all fees for the current institute
	@GetMapping("/list")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> getFeesList(Principal principal) {
		User user = userRepository.findByUsername(principal.getName())
				.orElseThrow(() -> new RuntimeException("User not found"));

		List<InstituteRegistrationFeeResponseDTO> fees = feeService.getFeesByUserId(user.getUserId());

		return ResponseEntity.ok(createSuccessResponse("Fees retrieved successfully", fees));
	}

	// Get fee by caste
	@GetMapping("/fee-by-caste/{caste}")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> getFeeByCAste(Principal principal, @PathVariable Caste caste) {
		User user = userRepository.findByUsername(principal.getName())
				.orElseThrow(() -> new RuntimeException("User not found"));

		Optional<InstituteRegistrationFeeResponseDTO> fee = feeService.getFeeByUserIdAndCaste(user.getUserId(), caste);

		if (fee.isPresent()) {
			return ResponseEntity.ok(createSuccessResponse("Fee retrieved successfully", fee.get()));
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(createErrorResponse("Fee not found for this caste"));
		}
	}

	// Update registration fee
	@PutMapping("/update/{feeId}")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> updateFee(@PathVariable Integer feeId,
			@Valid @RequestBody InstituteRegistrationFeeRequestDTO feeDTO, BindingResult bindingResult,
			Principal principal) {
		if (bindingResult.hasErrors()) {
			String errorMessage = getValidationErrorMessage(bindingResult);
			return ResponseEntity.badRequest().body(createErrorResponse("Validation error: " + errorMessage));
		}

		InstituteRegistrationFeeResponseDTO updatedFee = feeService.updateFee(feeId, feeDTO);

		return ResponseEntity.ok(createSuccessResponse("Registration fee updated successfully", updatedFee));

	}

	// Delete (soft delete) registration fee
	@DeleteMapping("/delete/{feeId}")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> deleteFee(@PathVariable Integer feeId) {
		feeService.deleteFee(feeId);
		return ResponseEntity.ok(createSuccessResponse("Registration fee deleted successfully", null));
	}

	// Check if caste category already exists
	@GetMapping("/exists/{caste}")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> checkCasteExists(Principal principal, @PathVariable Caste caste) {
		User user = userRepository.findByUsername(principal.getName())
				.orElseThrow(() -> new RuntimeException("User not found"));

		boolean exists = feeService.isFeeCategoryExists(user.getUserId(), caste);

		return ResponseEntity.ok(
				createSuccessResponse(exists ? "Caste category already exists" : "Caste category available", exists));
	}

	// Helper method to extract validation error messages
	private String getValidationErrorMessage(BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			FieldError fieldError = bindingResult.getFieldError();
			if (fieldError != null && fieldError.getDefaultMessage() != null) {
				return fieldError.getDefaultMessage();
			}
			return "Validation failed";
		}
		return "Validation failed";
	}

	// Helper method to create success response
	private Map<String, Object> createSuccessResponse(String message, Object data) {
		Map<String, Object> response = new HashMap<>();
		response.put("success", true);
		response.put("message", message);
		response.put("data", data);
		return response;
	}

	// Helper method to create error response
	private Map<String, Object> createErrorResponse(String message) {
		Map<String, Object> response = new HashMap<>();
		response.put("success", false);
		response.put("message", message);
		response.put("data", null);
		return response;
	}
}