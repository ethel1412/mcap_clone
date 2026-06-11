package nic.meg.mcap.controllers;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import nic.meg.mcap.dto.request.SubjectRequestDTO;
import nic.meg.mcap.dto.response.SubjectResponseDTO;
import nic.meg.mcap.entities.ProgrammeOffered;
import nic.meg.mcap.entities.Subject;
import nic.meg.mcap.enums.SubjectType;
import nic.meg.mcap.repositories.ProgrammesOfferedRepository;
import nic.meg.mcap.services.SubjectService;

@RestController
@RequestMapping("/subject-data")
//@PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
@Validated
public class SubjectDataController {

	@Autowired
	private SubjectService subjectService;
	@Autowired
	private ProgrammesOfferedRepository programmesOfferedRepository;

	private SubjectResponseDTO convertToDTO(Subject subject) {
		SubjectResponseDTO dto = new SubjectResponseDTO();
		dto.setSubjectId(subject.getSubjectId());
		dto.setSubjectName(subject.getSubjectName());
		dto.setSubjectCode(subject.getSubjectCode());
		return dto;
	}

	@GetMapping
	public ResponseEntity<List<SubjectResponseDTO>> getAllSubjects() {
		List<Subject> subjects = subjectService.getAllSubjects();
		List<SubjectResponseDTO> dtos = subjects.stream().map(this::convertToDTO).collect(Collectors.toList());
		return ResponseEntity.ok(dtos);
	}

	@GetMapping("/{id}")
	public ResponseEntity<SubjectResponseDTO> getSubjectById(@PathVariable Integer id) {
		Subject subject = subjectService.getSubjectById(id)
				.orElseThrow(() -> new EntityNotFoundException("Subject not found with ID: " + id));
		return ResponseEntity.ok(convertToDTO(subject));
	}

	@PostMapping
	public ResponseEntity<SubjectResponseDTO> createSubject(@Valid @RequestBody SubjectRequestDTO requestDTO) {
		Subject subject = new Subject();
		subject.setSubjectName(requestDTO.getSubjectName());
		subject.setSubjectCode(requestDTO.getSubjectCode());

		Subject created = subjectService.createSubject(subject);
		return new ResponseEntity<>(convertToDTO(created), HttpStatus.CREATED);
	}

	@PutMapping("/{id}")
	public ResponseEntity<SubjectResponseDTO> updateSubject(@PathVariable Integer id,
			@Valid @RequestBody SubjectRequestDTO requestDTO) {
		Subject subject = new Subject();
		subject.setSubjectName(requestDTO.getSubjectName());
		subject.setSubjectCode(requestDTO.getSubjectCode());

		Subject updated = subjectService.updateSubject(id, subject);
		return ResponseEntity.ok(convertToDTO(updated));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteSubject(@PathVariable Integer id) {
		subjectService.deleteSubject(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/search")
	public ResponseEntity<List<SubjectResponseDTO>> searchSubjects(@RequestParam String q) {
		List<Subject> subjects = subjectService.searchSubjects(q);
		List<SubjectResponseDTO> dtos = subjects.stream().map(this::convertToDTO).collect(Collectors.toList());
		return ResponseEntity.ok(dtos);
	}

	@GetMapping("/check-name")
	public ResponseEntity<Boolean> checkSubjectNameExists(@RequestParam String name) {
		boolean exists = subjectService.existsBySubjectName(name);
		return ResponseEntity.ok(exists);
	}

	@GetMapping("/by-stream")
	public ResponseEntity<List<SubjectResponseDTO>> getSubjectsByStream(@RequestParam Short streamId) {
		List<Subject> subjects = subjectService.findByStream(streamId);
		List<SubjectResponseDTO> dto = subjects.stream().map(this::convertToDTO) // Uses your existing private helper
																					// method
				.collect(Collectors.toList());
		return ResponseEntity.ok(dto);
	}

	@GetMapping("/by-type/{type}")
	public ResponseEntity<List<SubjectResponseDTO>> getSubjectsByType(@PathVariable("type") String type) {
		try {
			SubjectType subjectType = SubjectType.from(type.toUpperCase());
			List<Subject> subjects = subjectService.findBySubjectType(subjectType); // We will create this service
																					// method next

			List<SubjectResponseDTO> dtos = subjects.stream().map(this::convertToDTO).collect(Collectors.toList());

			return ResponseEntity.ok(dtos);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().build();
		}
	}

	@GetMapping("/by-all-types")
	public ResponseEntity<Map<SubjectType, List<SubjectResponseDTO>>> getAllSubjectsGroupedByType() {
		// 1. Get the raw data from the service
		Map<SubjectType, List<Subject>> groupedSubjects = subjectService.getAllSubjectsGroupedByType();

		// 2. Convert the inner lists to DTOs
		Map<SubjectType, List<SubjectResponseDTO>> response = groupedSubjects.entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey,
						entry -> entry.getValue().stream().map(this::convertToDTO).collect(Collectors.toList())));

		return ResponseEntity.ok(response);
	}

	@GetMapping("/for-programme-stream/{programmeOfferedId}")
	@PreAuthorize("hasRole('INSTITUTE')")
	public ResponseEntity<List<SubjectResponseDTO>> getSubjectsForProgrammeStream(
			@PathVariable Integer programmeOfferedId) {

		if (programmeOfferedId == null || programmeOfferedId <= 0) {
			throw new IllegalArgumentException("Invalid programmeOfferedId");
		}

		ProgrammeOffered po = programmesOfferedRepository.findById(programmeOfferedId)
				.orElseThrow(() -> new EntityNotFoundException("Programme Offered not found."));

		List<Subject> subjects = subjectService.getAllSubjects();

		List<SubjectResponseDTO> dtos = subjects.stream().map(subject -> {
			SubjectResponseDTO dto = new SubjectResponseDTO();
			dto.setSubjectId(subject.getSubjectId());
			dto.setSubjectName(subject.getSubjectName());
			dto.setSubjectCode(subject.getSubjectCode());
			dto.setSubjectType(subject.getSubjectType() != null ? subject.getSubjectType().name() : null);

			return dto;
		}).collect(Collectors.toList());

		return ResponseEntity.ok(dtos);
	}
}