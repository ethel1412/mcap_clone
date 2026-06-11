package nic.meg.mcap.controllers;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import nic.meg.mcap.dto.request.ProgrammeOfferedBatchAssignRequestDTO;
import nic.meg.mcap.dto.request.ProgrammeOfferedRequestDTO;
import nic.meg.mcap.dto.response.ProgrammeOfferedResponseDTO;
import nic.meg.mcap.dto.response.ProgrammeResponseDTO;
import nic.meg.mcap.entities.Programme;
import nic.meg.mcap.entities.ProgrammeOffered;
import nic.meg.mcap.enums.ProgrammeLevel;
import nic.meg.mcap.repositories.ProgrammesOfferedRepository;
import nic.meg.mcap.services.InstituteService;
import nic.meg.mcap.services.ProgrammeOfferedService;

@RestController
@RequestMapping("/programmes-offered/data")
@PreAuthorize("hasAnyRole('ADMIN','INSTITUTE')")
public class ProgrammeOfferedDataController {

	@Autowired
	private ProgrammeOfferedService programmeOfferedService;

	@Autowired
	private InstituteService instituteService;

	@Autowired
	private ProgrammesOfferedRepository programmesOfferedRepository;

	private static final Logger logger = LoggerFactory.getLogger(ProgrammeOfferedDataController.class);

	@GetMapping
	public ResponseEntity<List<ProgrammeOfferedResponseDTO>> getAllProgrammesOffered() {
		List<ProgrammeOfferedResponseDTO> responseList = programmeOfferedService.getAllProgrammesOffered();
		return ResponseEntity.ok(responseList);
	}

	@PostMapping
	public ResponseEntity<?> createProgrammeOffered(@Valid @RequestBody ProgrammeOfferedRequestDTO requestDTO,
			Principal principal) {
		try {
			Short instituteId = instituteService.findInstituteIdByUsername(principal.getName());
			List<ProgrammeOfferedResponseDTO> response = programmeOfferedService.createProgrammeOffered(requestDTO,
					instituteId);
			return ResponseEntity.ok(response);
		} catch (SecurityException e) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
		}
	}

	@GetMapping("/{id}")
	public ResponseEntity<List<ProgrammeOfferedResponseDTO>> getProgrammeOfferedById(@PathVariable Integer id) {
		List<ProgrammeOfferedResponseDTO> response = programmeOfferedService.getProgrammeOfferedById(id);
		return ResponseEntity.ok(response);
	}

	@PutMapping("/{id}")
	public ResponseEntity<?> updateProgrammeOffered(@PathVariable Integer id,
			@Valid @RequestBody ProgrammeOfferedRequestDTO requestDTO, Principal principal) {
		try {
			Short instituteId = instituteService.findInstituteIdByUsername(principal.getName());

			ProgrammeOfferedResponseDTO response = programmeOfferedService.updateProgrammeOffered(id, requestDTO,
					instituteId);

			return ResponseEntity.ok(response);

		} catch (SecurityException e) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
		}
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteProgrammeOffered(@PathVariable Integer id,
			@RequestParam(defaultValue = "single") String shiftType, Principal principal) {
		if (id == null || id <= 0 || id > 9999999) {
			return ResponseEntity.badRequest().body(Map.of("message", "Invalid ID provided"));
		}

		if (!"single".equalsIgnoreCase(shiftType) && !"all".equalsIgnoreCase(shiftType)) {
			return ResponseEntity.badRequest().body(Map.of("message", "Invalid shiftType. Allowed: single, all"));
		}
		Short instituteId = instituteService.findInstituteIdByUsername(principal.getName());

		if ("all".equalsIgnoreCase(shiftType)) {
			programmeOfferedService.deleteAllShifts(id, instituteId);
		} else {
			programmeOfferedService.deleteProgrammeOffered(id, instituteId);
		}

		Map<String, String> response = new HashMap<>();
		response.put("message", "Programme removed successfully");

		return ResponseEntity.ok(response);
	}

	@GetMapping("/institute/{instituteId}/")
	public ResponseEntity<List<ProgrammeOfferedResponseDTO>> listProgrammesByInstituteDepartment(
			@PathVariable Short instituteId) {
		List<ProgrammeOfferedResponseDTO> responseList = programmeOfferedService.listProgrammesByInstitute(instituteId);
		return ResponseEntity.ok(responseList);
	}

	@PostMapping("/batch-assign")
	public ResponseEntity<?> assignMultipleProgrammesToDepartment(
			@Valid @RequestBody ProgrammeOfferedBatchAssignRequestDTO batchRequest, Principal principal) {
		Short instituteId = instituteService.findInstituteIdByUsername(principal.getName());
		programmeOfferedService.assignMultipleProgrammesToDepartment(batchRequest, instituteId);
		return ResponseEntity.noContent().build();

	}

	@GetMapping("/by-programme-name")
	public ResponseEntity<List<ProgrammeOfferedResponseDTO>> getProgrammesOfferedByProgrammeName(
			@RequestParam String name) {
		List<ProgrammeOfferedResponseDTO> list = programmeOfferedService.getProgrammesOfferedByProgrammeName(name);
		return ResponseEntity.ok(list);
	}

	@GetMapping("/my")
	public ResponseEntity<List<ProgrammeOfferedResponseDTO>> getProgrammesOfferedByLoggedInInstitute(
			Principal principal) {
		Short instituteId = instituteService.findInstituteIdByUsername(principal.getName());
		List<ProgrammeOfferedResponseDTO> list = programmeOfferedService.listProgrammesByInstitute(instituteId);
		return ResponseEntity.ok(list);
	}

	@GetMapping("/institute/{instituteId}/stream/{streamId}")
	public ResponseEntity<List<ProgrammeOfferedResponseDTO>> getProgrammesOfferedByInstituteAndStream(
			@PathVariable Short instituteId, @PathVariable Short streamId) {
		List<ProgrammeOfferedResponseDTO> programmes = programmeOfferedService
				.getProgrammesOfferedByInstituteAndStream(instituteId, streamId);
		return ResponseEntity.ok(programmes);
	}

	@GetMapping("/by-stream/{streamId}")
	@PreAuthorize("permitAll")
	public ResponseEntity<List<ProgrammeResponseDTO>> getProgrammesByStream(@PathVariable Short streamId) {
		List<ProgrammeOffered> programmeOffered = programmesOfferedRepository
				.findDistinctProgrammesByStreamId(streamId);

		List<ProgrammeResponseDTO> programmes = programmeOffered.stream().map(po -> {
			Programme programme = po.getProgramme();
			ProgrammeResponseDTO dto = new ProgrammeResponseDTO();
			dto.setProgrammeId(programme.getProgrammeId());
			dto.setProgrammeName(programme.getProgrammeName());
			dto.setProgrammeLevel(programme.getProgrammeLevel());
			dto.setStreamId(programme.getStream().getStreamId());
			dto.setStreamName(programme.getStream().getStreamName());
			return dto;
		}).distinct().collect(Collectors.toList());

		return ResponseEntity.ok(programmes);
	}

	@GetMapping("/institute/programme-levels")
	public ResponseEntity<List<String>> getProgrammeLevelsForLoggedInInstitute(Principal principal) {
		Short instituteId = instituteService.findInstituteIdByUsername(principal.getName());
		List<String> levels = programmeOfferedService.findDistinctProgrammeLevelsByInstitute(instituteId);
		return ResponseEntity.ok(levels);
	}

	@GetMapping("/institute/by-level/{level}")
	public ResponseEntity<?> getByLevel(@PathVariable ProgrammeLevel level, Principal principal) {
		String username = principal.getName();
		Short instituteId = instituteService.getInstituteIdByUsername(username);
		return ResponseEntity.ok(programmeOfferedService.findProgrammesByLevelAndInstitute(level, instituteId));
	}
}