package nic.meg.mcap.controllers;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import nic.meg.mcap.dto.request.SeatReservationRequestDTO;
import nic.meg.mcap.dto.response.SeatReservationResponseDTO;
import nic.meg.mcap.entities.ProgrammeOffered;
import nic.meg.mcap.services.InstituteService;
import nic.meg.mcap.services.ProgrammeOfferedService;
import nic.meg.mcap.services.SeatReservationService;

@RestController
@RequestMapping("/seat-reservations")
@PreAuthorize("hasRole('INSTITUTE')")
public class SeatReservationDataController {

	private final SeatReservationService reservationService;
	private final InstituteService instituteService;
	private final ProgrammeOfferedService programmeOfferedService;

	public SeatReservationDataController(SeatReservationService reservationService, InstituteService instituteService,
			ProgrammeOfferedService programmeOfferedService) {
		this.reservationService = reservationService;
		this.instituteService = instituteService;
		this.programmeOfferedService = programmeOfferedService;
	}

	/**
	 * Create a new seat reservation
	 */
	@PostMapping("/create")
	public ResponseEntity<?> createReservation(@Valid @RequestBody SeatReservationRequestDTO requestDTO,
			Principal principal) {
		try {
			Short loggedInInstituteId = instituteService.findInstituteIdByUsername(principal.getName());

			ProgrammeOffered programmeOffered = programmeOfferedService.findById(requestDTO.getProgrammeOfferedId())
					.orElseThrow(() -> new IllegalArgumentException("Programme not found"));

			Short programmeInstituteId = programmeOffered.getInstituteDepartment().getInstitute().getInstituteId();

			if (!programmeInstituteId.equals(loggedInInstituteId)) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN)
						.body(Map.of("message", "Unauthorized: You don't have access to this programme"));
			}

			if (requestDTO.getAdmissionWindowId() == null) {
				return ResponseEntity.badRequest().body(Map.of("message", "Admission window ID is required"));
			}

			requestDTO.setReservedSeats(null);

			SeatReservationResponseDTO response = reservationService.createReservation(requestDTO);

			return ResponseEntity.ok(response);

		} catch (IllegalStateException | IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
		}
	}

	/**
	 * Get all reservations for a programme
	 */
	@GetMapping("/data/{programmeOfferedId}")
	public ResponseEntity<?> getReservations(@PathVariable Integer programmeOfferedId, Principal principal) {
		// Security: Verify institute owns this programme
		Short loggedInInstituteId = instituteService.findInstituteIdByUsername(principal.getName());
		ProgrammeOffered programmeOffered = programmeOfferedService.findById(programmeOfferedId)
				.orElseThrow(() -> new IllegalArgumentException("Programme not found"));

		Short programmeInstituteId = programmeOffered.getInstituteDepartment().getInstitute().getInstituteId();

		if (!programmeInstituteId.equals(loggedInInstituteId)) {
			Map<String, String> error = new HashMap<>();
			error.put("message", "Unauthorized access");
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
		}

		List<SeatReservationResponseDTO> reservations = reservationService
				.getReservationsByProgrammeOffered(programmeOfferedId);
		return ResponseEntity.ok(reservations);
	}

	/**
	 * Delete a reservation
	 */
	@DeleteMapping("/delete/{reservationId}")
	public ResponseEntity<?> deleteReservation(@PathVariable Long reservationId,
			@RequestParam Integer programmeOfferedId, Principal principal) {
		// Security: Verify institute owns this programme
		Short loggedInInstituteId = instituteService.findInstituteIdByUsername(principal.getName());
		ProgrammeOffered programmeOffered = programmeOfferedService.findById(programmeOfferedId)
				.orElseThrow(() -> new IllegalArgumentException("Programme not found"));

		Short programmeInstituteId = programmeOffered.getInstituteDepartment().getInstitute().getInstituteId();

		if (!programmeInstituteId.equals(loggedInInstituteId)) {
			Map<String, String> error = new HashMap<>();
			error.put("message", "Unauthorized access");
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
		}

		// Delete reservation
		reservationService.deleteReservation(reservationId, programmeOfferedId);
		Map<String, String> response = new HashMap<>();
		response.put("message", "Reservation deleted successfully");
		return ResponseEntity.ok(response);

	}
}