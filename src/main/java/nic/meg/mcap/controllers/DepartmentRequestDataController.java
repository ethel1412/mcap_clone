package nic.meg.mcap.controllers;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import nic.meg.mcap.dto.request.DepartmentRequestDTO;
import nic.meg.mcap.dto.response.DepartmentRequestResponseDTO;
import nic.meg.mcap.repositories.DepartmentRequestRepository;
import nic.meg.mcap.services.DepartmentRequestService;
import nic.meg.mcap.services.InstituteService;

@RestController
@RequestMapping("/department-requests")
public class DepartmentRequestDataController {

	@Autowired
	private DepartmentRequestService requestService;

	@Autowired
	private InstituteService instituteService;

	@Autowired
	private DepartmentRequestRepository requestRepo;

	private static final Logger logger = LoggerFactory.getLogger(DepartmentRequestDataController.class);

	/**
	 * Submit a new Department Request. Role: INSTITUTE
	 */
	@PostMapping("/submit")
	@PreAuthorize("hasRole('INSTITUTE')")
	public ResponseEntity<?> submitRequest(@Valid @RequestBody DepartmentRequestDTO requestDTO, Principal principal) {

		// Get the logged-in Institute ID
		Short instituteId = instituteService.findInstituteIdByUsername(principal.getName());
		try {
			requestService.submitRequest(instituteId, requestDTO);

			Map<String, String> response = new HashMap<>();
			response.put("message", "Department request submitted successfully. Waiting for Controller approval.");
			return ResponseEntity.ok(response);

		} catch (Exception e) {
			Map<String, String> error = new HashMap<>();
			error.put("message", "Invalid request data."); // safer than exposing raw message
			return ResponseEntity.badRequest().body(error);

		}
	}

	/**
	 * Institute views the status of their own requests. Role: INSTITUTE
	 */
	@GetMapping("/my")
	@PreAuthorize("hasRole('INSTITUTE')")
	public ResponseEntity<List<DepartmentRequestResponseDTO>> getMyRequests(Principal principal) {
		Short instituteId = instituteService.findInstituteIdByUsername(principal.getName());
		List<DepartmentRequestResponseDTO> requests = requestService.getRequestsByInstitute(instituteId);
		return ResponseEntity.ok(requests);
	}

	/**
	 * Get all pending department requests for the Controller to review. Role:
	 * CONTROLLER
	 */
	@GetMapping("/controller/pending")
	@PreAuthorize("hasRole('CONTROLLER')")
	public ResponseEntity<List<DepartmentRequestResponseDTO>> getPendingRequests() {
		return ResponseEntity.ok(requestService.getAllPendingRequests());
	}

	/**
	 * Get ALL department requests (History + Pending). Role: CONTROLLER
	 */
	@GetMapping("/controller/all")
	@PreAuthorize("hasRole('CONTROLLER')")
	public ResponseEntity<List<DepartmentRequestResponseDTO>> getAllRequests() {
		return ResponseEntity.ok(requestService.getAllRequests());
	}

	/**
	 * Approve a department request. Role: CONTROLLER
	 */
	@PostMapping("/controller/{requestId}/approve")
	@PreAuthorize("hasRole('CONTROLLER')")
	public ResponseEntity<?> approveRequest(@PathVariable Long requestId) {
		try {
			requestService.approveRequest(requestId);

			Map<String, String> response = new HashMap<>();
			response.put("message", "Department approved and added to Institute's list.");
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			Map<String, String> error = new HashMap<>();
			error.put("error", e.getMessage());
			return ResponseEntity.badRequest().body(error);
		}
	}

	/**
	 * Reject a department request. Role: CONTROLLER
	 */
	@PostMapping("/controller/{requestId}/reject")
	@PreAuthorize("hasRole('CONTROLLER')")
	public ResponseEntity<?> rejectRequest(@PathVariable Long requestId, @RequestParam String reason) {

		requestService.rejectRequest(requestId, reason);

		Map<String, String> response = new HashMap<>();
		response.put("message", "Request rejected.");
		return ResponseEntity.ok(response);
	}

	/**
	 * Endpoint for INSTITUTE: Count my pending requests (for sidebar badge).
	 */
	@GetMapping("/my/count-pending")
	@PreAuthorize("hasRole('INSTITUTE')")
	public ResponseEntity<Long> getMyPendingCount(Principal principal) {
		Short instituteId = instituteService.findInstituteIdByUsername(principal.getName());
		return ResponseEntity.ok(requestRepo.countByInstitute_InstituteIdAndStatus(instituteId, "PENDING"));
	}

	/**
	 * Endpoint for CONTROLLER: Count all pending requests (for sidebar badge).
	 */
	@GetMapping("/controller/count-pending")
	@PreAuthorize("hasRole('CONTROLLER')")
	public ResponseEntity<Long> getAllPendingCount() {
		return ResponseEntity.ok(requestRepo.countByStatus("PENDING"));
	}
}