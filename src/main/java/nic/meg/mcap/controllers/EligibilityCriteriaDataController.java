package nic.meg.mcap.controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import nic.meg.mcap.dto.request.EligibilityCriteriaRequestDTO;
import nic.meg.mcap.dto.response.EligibilityCriteriaResponseDTO;
import nic.meg.mcap.entities.AdmissionWindow;
import nic.meg.mcap.entities.ProgrammeOffered;
import nic.meg.mcap.enums.Caste;
import nic.meg.mcap.enums.ProgrammeLevel;
import nic.meg.mcap.enums.SubjectType;
import nic.meg.mcap.repositories.AdmissionWindowRepository;
import nic.meg.mcap.repositories.CommunityCategoryRepository;
import nic.meg.mcap.repositories.CuetPaperRepository;
import nic.meg.mcap.repositories.ProgrammesOfferedRepository;
import nic.meg.mcap.repositories.QualificationRepository;
import nic.meg.mcap.repositories.SubjectRepository;
import nic.meg.mcap.services.EligibilityCriteriaService;
// --- ADDED IMPORT ---
import nic.meg.mcap.services.ScheduleHelperService;

@RestController
@RequestMapping("/api/data/eligibility")
@RequiredArgsConstructor
public class EligibilityCriteriaDataController {

	private final EligibilityCriteriaService eligibilityService;
	private final AdmissionWindowRepository windowRepo;
	private final ProgrammesOfferedRepository programmeRepo;
	private final SubjectRepository subjectRepo;
	private final CommunityCategoryRepository categoryRepo;
	private final QualificationRepository qualificationRepo;
	private final CuetPaperRepository cuetPaperRepository;
	private final ScheduleHelperService scheduleHelperService;
	private static final String ELIGIBILITY_STEP_NAME = "Set Eligibility Rules";
	private static final Logger logger = LoggerFactory.getLogger(EligibilityCriteriaDataController.class);

	@GetMapping("/master/cuet-subjects")
	public ResponseEntity<?> getCuetMasterSubjects(@RequestParam(required = false) String level) {
		ProgrammeLevel programmeLevel;
		try {
			programmeLevel = (level == null || level.isBlank()) ? ProgrammeLevel.UG
					: ProgrammeLevel.valueOf(level.trim().toUpperCase());

		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(Map.of("error", "Invalid level. Use UG or PG."));
		}

		var papers = cuetPaperRepository
				.findByProgrammeLevelAndIsActiveOrderBySpecAscSortOrderAscPaperNameAsc(programmeLevel, true);

		var response = papers.stream().map(p -> {
			Map<String, Object> m = new LinkedHashMap<>();
			m.put("paperCode", p.getPaperCode());
			m.put("displayName", p.getPaperName()); // keep UI field name displayName
			m.put("paperName", p.getPaperName()); // optional, but handy
			m.put("domainName", p.getDomainName());
			m.put("spec", p.getSpec() != null ? p.getSpec().name() : null);
			m.put("sortOrder", p.getSortOrder());
			return m;
		}).toList();

		return ResponseEntity.ok(response);
	}

	@GetMapping("/master/subjects")
	public ResponseEntity<?> getAllSubjects() {
		var response = subjectRepo.findBySubjectType(SubjectType.GENERAL).stream()
				.map(s -> Map.of("subjectId", s.getSubjectId(), "subjectName", s.getSubjectName())).toList();

		return ResponseEntity.ok(response);
	}

	@GetMapping("/master/qualifications")
	public ResponseEntity<?> getQualifications() {
		var response = qualificationRepo.findAll().stream()
				.map(q -> Map.of("id", q.getId(), "name", q.getName(), "isActive", q.isActive())).toList();
		return ResponseEntity.ok(response);
	}

	@GetMapping("/master/categories")
	public ResponseEntity<?> getAllCategories() {
		var response = categoryRepo.findAll().stream()
				.map(c -> Map.of("categoryCode", c.getCategoryCode(), "categoryName", c.getCategoryName())).toList();
		return ResponseEntity.ok(response);
	}

	@GetMapping("/master/castes")
	public ResponseEntity<?> getAllCastes() {
		List<Map<String, Object>> castes = Arrays.stream(Caste.values()).map(c -> {
			Map<String, Object> map = new HashMap<>();
			map.put("categoryCode", c.getCategoryCode());
			map.put("displayName", c.getDisplayName());
			map.put("priority", c.getPriority());
			return map;
		}).toList();
		return ResponseEntity.ok(castes);
	}

	@GetMapping("/windows")
	public ResponseEntity<List<Map<String, Object>>> getAllWindows() {

		List<AdmissionWindow> allWindows = windowRepo.findAll();
		List<Map<String, Object>> response = new ArrayList<>();

		for (AdmissionWindow w : allWindows) {

			boolean isEligibilityOpen = scheduleHelperService.isWindowInScheduleStep(w.getAdmissionCode(),
					ELIGIBILITY_STEP_NAME);

			String status = isEligibilityOpen ? "OPEN" : "CLOSED";

			long pending;

			if (w.getStream() == null) {
				pending = programmeRepo.findByProgramme_ProgrammeLevel(w.getProgrammeLevel()).stream()
						.filter(p -> eligibilityService.getCriteriaByWindowAndProgramme(w.getAdmissionCode(),
								p.getProgramme().getProgrammeId() // ✅ FIX
						) == null).count();

			} else {
				List<Short> streamIds = (w.getStream() != null && w.getStream().getStreamId() != null)
						? List.of(w.getStream().getStreamId())
						: List.of((short) 101, (short) 102, (short) 103);

				pending = programmeRepo.findByStreamProgramme(streamIds, w.getProgrammeLevel()).stream()
						.filter(p -> eligibilityService.getCriteriaByWindowAndProgramme(w.getAdmissionCode(),
								p.getProgramme().getProgrammeId()) == null)
						.count();
			}

			Map<String, Object> item = new HashMap<>();

			item.put("admissionCode", w.getAdmissionCode());

			String streamName = (w.getStream() != null) ? w.getStream().getStreamName() : "All Streams";

			item.put("name", streamName + " - " + w.getProgrammeLevel() + " - " + w.getSession());

			item.put("scheduleStatus", status);
			item.put("pendingCount", pending);
			item.put("startDate", w.getStartDate());
			item.put("endDate", w.getEndDate());

			response.add(item);
		}

		return ResponseEntity.ok(response);
	}

	// CHANGED: {windowId} to {admissionWindowCode}
	@GetMapping("/programmes-by-window/{admissionWindowCode}")
	public ResponseEntity<List<Map<String, Object>>> getProgrammesByWindow(
			@PathVariable("admissionWindowCode") String admissionWindowCode) {

		// Fetch window by code
		AdmissionWindow window = windowRepo.findByAdmissionCode(admissionWindowCode).orElseThrow();

		Short streamId = (window.getStream() != null) ? window.getStream().getStreamId() : null;

		// ✅ SAFE: pass streamId (can be null)
		List<Short> streamIds = getMappedStreamIds(streamId);

		List<ProgrammeOffered> programmes = programmeRepo.findByStreamIdsAndLevel(streamIds,
				window.getProgrammeLevel());

		Map<Short, Map<String, Object>> unique = new LinkedHashMap<>();

		for (ProgrammeOffered p : programmes) {
			Short programmeId = p.getProgramme().getProgrammeId();

			unique.computeIfAbsent(programmeId, id -> {
				Map<String, Object> map = new HashMap<>();

				map.put("programmeId", programmeId);
				map.put("programmeName", p.getProgramme().getProgrammeName());
				map.put("streamName",
						p.getProgramme().getStream() != null ? p.getProgramme().getStream().getStreamName() : "N/A");
				map.put("programmeLevel", window.getProgrammeLevel().toString());

				map.put("hasCriteria",
						eligibilityService.getCriteriaByWindowAndProgramme(admissionWindowCode, programmeId) != null);

				return map;
			});
		}

		return ResponseEntity.ok(new ArrayList<>(unique.values()));
	}

	@GetMapping("config")
	public ResponseEntity<EligibilityCriteriaResponseDTO> getCriteria(
			// CHANGED: Short admissionWindowId to String admissionWindowCode
			@RequestParam("admissionWindowCode") String admissionWindowCode, @RequestParam Short programmeId) {
		// CHANGED: Passed code to service
		EligibilityCriteriaResponseDTO response = eligibilityService
				.getCriteriaByWindowAndProgramme(admissionWindowCode, programmeId);
		return response != null ? ResponseEntity.ok(response) : ResponseEntity.noContent().build();
	}

	@PostMapping("/save")
	public ResponseEntity<EligibilityCriteriaResponseDTO> saveCriteria(
			@RequestBody EligibilityCriteriaRequestDTO requestDTO) {
		return ResponseEntity.ok(eligibilityService.saveCriteria(requestDTO));
	}

	private List<Short> getMappedStreamIds(Short streamId) {
		if (streamId == null) {
			return List.of((short) 101, (short) 102, (short) 103);
		}
		return List.of(streamId);
	}
}