package nic.meg.mcap.controllers;

import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import nic.meg.mcap.services.InstituteService;

@RestController
@RequestMapping("/api/validate")
public class ValidationController {

	@Autowired
	private InstituteService instituteService;

	@JsonIgnoreProperties(ignoreUnknown = false)
	public record ValidationRequest(

			@NotBlank(message = "AISHE ID is required")

			@jakarta.validation.constraints.Pattern(regexp = "^[A-Z]-\\d{5}$", message = "AISHE ID must be in format X-12345") String value,
			Short instituteId

	) {
	}

	public record ValidationResponse(boolean isUnique, String message) {
	}

	@PostMapping("/aisheId")
	public ResponseEntity<ValidationResponse> checkAisheId(@RequestBody @Valid ValidationRequest request) {
		String aisheId = request.value();
		Short currentInstituteId = request.instituteId();

		// Trim the value first to remove leading/trailing spaces
		if (aisheId != null) {
			aisheId = aisheId.trim();
		}

		// 1. AISHE ID is now required - reject empty values
		if (aisheId == null || aisheId.isEmpty()) {
			return ResponseEntity.ok(new ValidationResponse(false, "AISHE ID is required"));
		}

		// 2. Convert to uppercase for consistent format validation
		String uppercaseAisheId = aisheId.toUpperCase();

		// 3. Perform format validation for non-empty AISHE ID
		Pattern aisheIdPattern = Pattern.compile("^[A-Z]-\\d{5}$");
		if (!aisheIdPattern.matcher(uppercaseAisheId).matches()) {
			return ResponseEntity
					.ok(new ValidationResponse(false, "AISHE ID must be in the format X-DDDDD (e.g., U-12345)"));
		}

		// 4. If format is valid, proceed with uniqueness check using the service
		boolean isUnique = instituteService.isAisheIdUnique(uppercaseAisheId, currentInstituteId);
		String message = isUnique ? "" : "This AISHE ID is already registered.";
		return ResponseEntity.ok(new ValidationResponse(isUnique, message));
	}

	@PostMapping("/email")
	public ResponseEntity<ValidationResponse> checkEmail(@RequestBody ValidationRequest request) {
		String email = (request.value() != null) ? request.value().trim() : null; // Trim for consistency
		Short currentInstituteId = request.instituteId();
		boolean isUnique = instituteService.isEmailUnique(email, currentInstituteId);
		String message = isUnique ? "" : "This Email address is already registered.";
		return ResponseEntity.ok(new ValidationResponse(isUnique, message));
	}

	@PostMapping("/contact")
	public ResponseEntity<ValidationResponse> checkContactNumber(@RequestBody ValidationRequest request) {
		String contactNumber = (request.value() != null) ? request.value().trim() : null; // Trim for consistency
		Short currentInstituteId = request.instituteId();
		boolean isUnique = instituteService.isContactNumberUnique(contactNumber, currentInstituteId);
		String message = isUnique ? "" : "This Contact Number is already registered.";
		return ResponseEntity.ok(new ValidationResponse(isUnique, message));
	}

	@PostMapping("/website")
	public ResponseEntity<ValidationResponse> checkWebsite(@RequestBody ValidationRequest request) {
		String website = (request.value() != null) ? request.value().trim() : null; // Trim for consistency
		Short currentInstituteId = request.instituteId();
		boolean isUnique = instituteService.isWebsiteUnique(website, currentInstituteId);
		String message = isUnique ? "" : "This Website is already registered.";
		return ResponseEntity.ok(new ValidationResponse(isUnique, message));
	}
}