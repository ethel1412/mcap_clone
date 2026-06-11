package nic.meg.mcap.controllers;

import java.util.Arrays;
import java.util.List;

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
import lombok.RequiredArgsConstructor;
import nic.meg.mcap.dto.request.ScheduleStepTemplateRequestDTO;
import nic.meg.mcap.dto.response.ScheduleStepTemplateDTO;
import nic.meg.mcap.dto.response.StepPresetDTO;
import nic.meg.mcap.enums.StepPreset;
import nic.meg.mcap.services.ScheduleStepTemplateService;

@RestController
@PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN','CONTROLLER')")
@RequestMapping("/step-template-data")
@RequiredArgsConstructor
public class ScheduleStepTemplateController {

	private final ScheduleStepTemplateService templateService;

	// --- GET TEMPLATES ---
	@GetMapping("/templates")
	public ResponseEntity<List<ScheduleStepTemplateDTO>> getAllTemplates(
			@RequestParam(required = false) String category) {
		if (category != null && !category.trim().isEmpty()) {
			return ResponseEntity.ok(templateService.getTemplatesByCategory(category));
		}
		return ResponseEntity.ok(templateService.getAllActiveTemplates());
	}

	@GetMapping("/templates/{templateId}")
	public ResponseEntity<ScheduleStepTemplateDTO> getTemplateById(@PathVariable Long templateId) {
		return ResponseEntity.ok(templateService.getTemplateById(templateId));
	}

	// --- MANUAL CRUD OPERATIONS ---
	@PostMapping("/templates")
	public ResponseEntity<ScheduleStepTemplateDTO> createTemplate(
			@Valid @RequestBody ScheduleStepTemplateRequestDTO dto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(templateService.createTemplate(dto));
	}

	@PutMapping("/templates/{templateId}")
	public ResponseEntity<ScheduleStepTemplateDTO> updateTemplate(@PathVariable Long templateId,
			@Valid @RequestBody ScheduleStepTemplateRequestDTO dto) {
		return ResponseEntity.ok(templateService.updateTemplate(templateId, dto));
	}

	@DeleteMapping("/templates/{templateId}")
	public ResponseEntity<Void> deleteTemplate(@PathVariable Long templateId) {
		templateService.deleteTemplate(templateId);
		return ResponseEntity.noContent().build();
	}

	// --- SMART GENERATORS ---

	@PostMapping("/auto-generate/pre-admission")
	public ResponseEntity<String> generatePreAdmission() {
		try {
			templateService.autoGeneratePreAdmissionSteps();
			return ResponseEntity.ok("Pre-Admission steps generated successfully.");
		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	@PostMapping("/auto-generate/counselling")
	public ResponseEntity<String> generateCounsellingPhase(@RequestParam String route) {
		templateService.addCounsellingPhaseCluster(route);

		return ResponseEntity.ok(route + " Counselling Phase generated successfully.");
	}

	// --- UTILS ---

	@GetMapping("/presets")
	public List<StepPresetDTO> getPresets() {
		return Arrays.stream(StepPreset.values())
				.map(p -> new StepPresetDTO(p.name(), p.getLabel(), p.getDefaultRole().name(), p.getDescription()))
				.toList();
	}

	@PostMapping("/finalize")
	public ResponseEntity<Void> finalizeRoadmap() {
		return ResponseEntity.ok().build();
	}
}