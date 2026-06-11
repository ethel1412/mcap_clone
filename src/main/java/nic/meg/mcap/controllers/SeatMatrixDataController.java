package nic.meg.mcap.controllers;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import nic.meg.mcap.dto.request.SeatMatrixRequestDTO;
import nic.meg.mcap.dto.response.SeatMatrixResponseDTO;
import nic.meg.mcap.services.InstituteService;
import nic.meg.mcap.services.SeatMatrixService;

@RestController
@RequestMapping("/seat-matrix/data")
public class SeatMatrixDataController {

	private final SeatMatrixService seatMatrixService;
	private final InstituteService instituteService;

	public SeatMatrixDataController(SeatMatrixService seatMatrixService, InstituteService instituteService) {
		this.seatMatrixService = seatMatrixService;
		this.instituteService = instituteService;
	}

	// Get all seat matrices for logged-in institute
	@GetMapping("/my")
	public ResponseEntity<List<SeatMatrixResponseDTO>> getSeatMatricesForLoggedInInstitute(Principal principal) {
		Short instituteId = instituteService.findInstituteIdByUsername(principal.getName());
		List<SeatMatrixResponseDTO> response = seatMatrixService.getByInstitute(instituteId);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/assign")
	public ResponseEntity<?> assignSeats(Principal principal, @Valid @RequestBody SeatMatrixRequestDTO request) {
		try {
			Short loggedInInstituteId = instituteService.findInstituteIdByUsername(principal.getName());

			SeatMatrixResponseDTO result = seatMatrixService.createOrUpdateSeatMatrix(request, loggedInInstituteId);

			return ResponseEntity.ok(result);

		} catch (SecurityException e) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));

		} catch (IllegalStateException e) {
			return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
		}
	}
}