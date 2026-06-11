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

import nic.meg.mcap.dto.request.ProgrammeRequestDTO;
import nic.meg.mcap.dto.response.ProgrammeRequestResponseDTO;
import nic.meg.mcap.repositories.ProgrammeRequestRepository;
import nic.meg.mcap.services.InstituteService;
import nic.meg.mcap.services.ProgrammeRequestService;

@RestController
@RequestMapping("/programme-requests")
public class ProgrammeRequestDataController {

	@Autowired
	private ProgrammeRequestService requestService;

	@Autowired
	private InstituteService instituteService;

	@Autowired
	private ProgrammeRequestRepository requestRepo;

	private static final Logger logger = LoggerFactory.getLogger(ProgrammeRequestDataController.class);

	@PostMapping("/submit")
	@PreAuthorize("hasRole('INSTITUTE')")
	public ResponseEntity<?> submitRequest(@RequestBody ProgrammeRequestDTO requestDTO, Principal principal) {
		try {
			Short instituteId = instituteService.findInstituteIdByUsername(principal.getName());

			requestService.submitRequest(instituteId, requestDTO);

			return ResponseEntity.ok(Map.of("message", "Request submitted successfully"));

		} catch (IllegalArgumentException | IllegalStateException e) {
			return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
		}
	}

	/**
	 * Institute views the status of their own requests.
	 */
	@GetMapping("/my")
	@PreAuthorize("hasRole('INSTITUTE')")
	public ResponseEntity<List<ProgrammeRequestResponseDTO>> getMyRequests(Principal principal) {
		Short instituteId = instituteService.findInstituteIdByUsername(principal.getName());
		List<ProgrammeRequestResponseDTO> requests = requestService.getRequestsByInstitute(instituteId);
		return ResponseEntity.ok(requests);
	}

	@GetMapping("/admin/pending")
	@PreAuthorize("hasRole('CONTROLLER')")
	public ResponseEntity<List<ProgrammeRequestResponseDTO>> getPendingRequests() {
		return ResponseEntity.ok(requestService.getAllPendingRequests());
	}

	/**
	 * Approve a request. RESTRICTED TO: CONTROLLER only.
	 */
	@PostMapping("/admin/{requestId}/approve")
	@PreAuthorize("hasRole('CONTROLLER')")
	public ResponseEntity<?> approveRequest(@PathVariable Long requestId) {
		try {
			requestService.approveRequest(requestId);

			return ResponseEntity.ok(Map.of("message", "Programme approved and added to Institute's offered list."));

		} catch (IllegalArgumentException | IllegalStateException e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}

	/**
	 * Reject a request. RESTRICTED TO: CONTROLLER only.
	 */
	@PostMapping("/admin/{requestId}/reject")
	@PreAuthorize("hasRole('CONTROLLER')")
	public ResponseEntity<?> rejectRequest(@PathVariable Long requestId, @RequestParam String reason) {

		requestService.rejectRequest(requestId, reason);

		Map<String, String> response = new HashMap<>();
		response.put("message", "Request rejected.");
		return ResponseEntity.ok(response);
	}

	// Endpoint for INSTITUTE: Count my pending requests
	@GetMapping("/my/count-pending")
	@PreAuthorize("hasRole('INSTITUTE')")
	public ResponseEntity<Long> getMyPendingCount(Principal principal) {
		Short instituteId = instituteService.findInstituteIdByUsername(principal.getName());
		return ResponseEntity.ok(requestRepo.countByInstitute_InstituteIdAndStatus(instituteId, "PENDING"));
	}

	// Endpoint for CONTROLLER: Count all pending requests
	@GetMapping("/admin/count-pending")
	@PreAuthorize("hasRole('CONTROLLER')")
	public ResponseEntity<Long> getAllPendingCount() {
		return ResponseEntity.ok(requestRepo.countByStatus("PENDING"));
	}

	@GetMapping("/admin/all")
	@PreAuthorize("hasRole('CONTROLLER')")
	public ResponseEntity<List<ProgrammeRequestResponseDTO>> getAllRequests() {
		return ResponseEntity.ok(requestService.getAllRequests());
	}

}