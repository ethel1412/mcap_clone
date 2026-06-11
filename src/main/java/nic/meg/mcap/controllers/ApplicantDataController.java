package nic.meg.mcap.controllers;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import nic.meg.mcap.dto.request.AcademicDetailsDTO;
import nic.meg.mcap.dto.request.ApplicantDTO;
import nic.meg.mcap.dto.request.PersonalDetailsRequestDTO;
import nic.meg.mcap.dto.request.ProgrammePreferenceRequestDTO;
import nic.meg.mcap.dto.request.RegistrationFormDTO;
import nic.meg.mcap.dto.response.ApplicationStatusResponseDTO;
import nic.meg.mcap.dto.response.ProgrammePreferenceResponseDTO;
import nic.meg.mcap.dto.response.ProgrammeResponseDTO;
import nic.meg.mcap.entities.AdmissionWindow;
import nic.meg.mcap.entities.Applicant;
import nic.meg.mcap.entities.Application;
import nic.meg.mcap.entities.Block;
import nic.meg.mcap.entities.District;
import nic.meg.mcap.entities.Document;
import nic.meg.mcap.entities.Institute;
import nic.meg.mcap.entities.Programme;
import nic.meg.mcap.entities.ProgrammeOffered;
import nic.meg.mcap.entities.Stream;
import nic.meg.mcap.entities.Subject;
import nic.meg.mcap.enums.InstituteStatus;
import nic.meg.mcap.enums.ProgrammeLevel;
import nic.meg.mcap.exception.ApplicationAlreadyExistsException;
import nic.meg.mcap.repositories.AdmissionWindowRepository;
import nic.meg.mcap.repositories.ApplicantRepository;
import nic.meg.mcap.repositories.ApplicationRepository;
import nic.meg.mcap.repositories.BlockRepository;
import nic.meg.mcap.repositories.DistrictRepository;
import nic.meg.mcap.repositories.ProgrammesOfferedRepository;
import nic.meg.mcap.repositories.StreamRepository;
import nic.meg.mcap.repositories.UserRepository;
import nic.meg.mcap.services.AcademicService;
import nic.meg.mcap.services.ApplicantService;
import nic.meg.mcap.services.ApplicationService;
import nic.meg.mcap.services.DocumentService;
import nic.meg.mcap.services.EligibilityCalculationService;
import nic.meg.mcap.services.InstituteRegistrationFeeService;
import nic.meg.mcap.services.ProgrammePreferenceService;

@Controller
@RequestMapping("/applicants")
public class ApplicantDataController {
	@Autowired
	private ApplicantService applicantService;
	@Autowired
	private AcademicService academicService;
	@Autowired
	private DistrictRepository districtRepository;
	@Autowired
	private BlockRepository blockRepository;
	@Autowired
	private DocumentService documentService;
	@Autowired
	private ApplicationRepository applicationRepository;
	@Autowired
	private ProgrammesOfferedRepository programmesOfferedRepository;
	@Autowired
	private ProgrammePreferenceService programmePreferenceService;
	@Autowired
	private InstituteRegistrationFeeService feeService;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ApplicantRepository applicantRepository;
	@Autowired
	private StreamRepository streamRepository;
	@Autowired
	private ApplicationService applicationService;
	@Autowired
	private EligibilityCalculationService eligibilityCalculationService;
	@Autowired
	private AdmissionWindowRepository admissionWindowRepository;

	private static final Logger logger = LoggerFactory.getLogger(ApplicantDataController.class);

	// THE FIX: @InitBinder methods have been completely removed to prevent Spring
	// from silently dropping form fields.

	@PostMapping("/register")
	@ResponseBody
	public ResponseEntity<?> processRegistration(@Valid @ModelAttribute("applicantDTO") RegistrationFormDTO formDTO,
			HttpServletRequest request, BindingResult result, @RequestParam("admissionCode") String admissionCode) {

		if (result.hasErrors()) {
			Map<String, String> errors = result.getFieldErrors().stream().collect(Collectors
					.toMap(fieldError -> fieldError.getField(), fieldError -> fieldError.getDefaultMessage()));
			return ResponseEntity.badRequest().body(Map.of("message", "Validation failed", "errors", errors));
		}
		try {

			AdmissionWindow window = admissionWindowRepository.findByAdmissionCode(admissionCode).orElse(null);

			ApplicantDTO savedApplicant = applicantService.registerApplicant(formDTO, window.getAdmissionId());

			return ResponseEntity.ok(Map.of("message", "Application Registered Successfully", "applicantNo",
					savedApplicant.getApplicantNo(), "isNewUser", savedApplicant.isNewUser()));

		} catch (ApplicationAlreadyExistsException e) {

			return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));

		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));

		} catch (Exception e) {
			return ResponseEntity.internalServerError()
					.body(Map.of("message", "We encountered a technical issue while processing your registration."));
		}
	}

	@PostMapping("/select-admission")
	public String selectAdmissionWindow(@RequestParam("admissionCode") String admissionCode,
			jakarta.servlet.http.HttpSession session, RedirectAttributes redirectAttributes,
			Authentication authentication) {

		if (admissionCode == null || !admissionCode.matches("^[A-Za-z0-9_-]{1,50}$")) {

			redirectAttributes.addFlashAttribute("error", "Invalid admission code.");

			return "redirect:/";
		}

		AdmissionWindow window = admissionWindowRepository.findByAdmissionCode(admissionCode).orElse(null);

		if (window == null || !window.isActive() || java.time.LocalDateTime.now().isAfter(window.getEndDate())) {
			redirectAttributes.addFlashAttribute("error",
					"The selected admission window is invalid or no longer open.");
			return "redirect:/";
		}

		session.setAttribute("admissionCode", admissionCode);
		return "redirect:/applicants/register";
	}

	@PostMapping("/academics/save")
	@ResponseBody
	public ResponseEntity<ApplicationStatusResponseDTO> saveAcademicDetails(
			@ModelAttribute("academicDetailsDTO") AcademicDetailsDTO dto, Authentication auth) {

		String applicantNo = auth.getName();
		try {
			academicService.saveOrUpdateAcademicDetails(applicantNo, dto);

			Application application = applicationRepository.findByApplicationNo(applicantNo)
					.orElseThrow(() -> new IllegalArgumentException("Applicant not found"));

			ApplicationStatusResponseDTO status = applicationService
					.updateAcademicDetailsStatus(application.getApplicationId(), applicantNo);

			return ResponseEntity.ok(status);

		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(null);

		} catch (org.springframework.dao.DataAccessException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GetMapping("/districts")
	public String getDistrictsByState(@RequestParam("stateCode") Short stateCode, Model model) {
		List<District> districts = districtRepository.findByState_StateCode(stateCode);
		model.addAttribute("districts", districts);
		return "fragments/options :: districtOptions";
	}

	@GetMapping("/blocks")
	public String getBlocksByDistrict(@RequestParam("districtCode") Short districtCode, Model model) {
		List<Block> blocks = blockRepository.findByDistrict_DistrictCode(districtCode);
		model.addAttribute("blocks", blocks);
		return "fragments/options :: blockOptions";
	}

	@PostMapping("/personal/update")
	@ResponseBody
	public ResponseEntity<ApplicationStatusResponseDTO> updatePersonalDetails(
			@RequestParam("applicationId") Long applicationId,
			@ModelAttribute("personalDetailsDTO") PersonalDetailsRequestDTO dto, Authentication auth) {
		String applicantNo = auth.getName();
		try {
			applicantService.updatePersonalDetails(applicantNo, dto);
			ApplicationStatusResponseDTO status = applicationService.updatePersonalDetailsStatus(applicationId,
					applicantNo);
			return ResponseEntity.ok(status);

		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(null);

		} catch (org.springframework.dao.DataAccessException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@PostMapping("/documents/upload")
	@ResponseBody
	public ResponseEntity<?> uploadDocument(@RequestParam("documentFile") MultipartFile file,
			@RequestParam("documentType") String documentType, Authentication auth) {
		String applicantNo = auth.getName();
		try {
			Document savedDoc = documentService.saveDocument(file, applicantNo, documentType);
			Map<String, Object> responseData = new HashMap<>();
			responseData.put("message", "File uploaded successfully.");
			responseData.put("documentId", savedDoc.getId());
			responseData.put("fileName", savedDoc.getFileName());
			return ResponseEntity.ok(responseData);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
		} catch (Exception e) {
			return ResponseEntity.internalServerError().body(Map.of("message",
					"The server could not process your file upload. Please ensure the file is not corrupt and try again."));
		}
	}

	@GetMapping("/documents/{documentId}")
	public ResponseEntity<byte[]> previewDocument(@PathVariable Long documentId, Authentication auth) {
		String username = auth.getName();
		Document document = documentService.getDocumentById(documentId)
				.orElseThrow(() -> new EntityNotFoundException("Document not found"));

		boolean isOwner = document.getApplicant().getApplicantNo().equals(username);
		boolean isInstitute = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_INSTITUTE"));

		if (!isOwner && !isInstitute) {
			throw new SecurityException("Unauthorized access to document");
		}

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.parseMediaType(document.getFileType()));
		headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + document.getFileName() + "\"");
		return new ResponseEntity<>(document.getFileContent(), headers, HttpStatus.OK);
	}

	@DeleteMapping("/documents/{documentId}")
	@ResponseBody
	public ResponseEntity<?> deleteDocument(@PathVariable Long documentId, Authentication auth) {
		String applicantNo = auth.getName();
		try {
			documentService.deleteDocument(documentId, applicantNo);
			return ResponseEntity.ok(Map.of("message", "Document deleted successfully."));
		} catch (SecurityException e) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
		} catch (EntityNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
		} catch (Exception e) {
			return ResponseEntity.internalServerError().body(Map.of("message", "An unexpected error occurred."));
		}
	}

	@PostMapping("/documents/finalize/{applicationId}")
	@ResponseBody
	public ResponseEntity<?> finalizeDocuments(@PathVariable Long applicationId, Authentication auth) {
		String applicantNo = auth.getName();
		try {
			documentService.validateAllRequiredDocumentsUploaded(applicantNo, applicationId);

			ApplicationStatusResponseDTO status = applicationService.updateDocumentsUploadStatus(applicationId,
					applicantNo);
			return ResponseEntity.ok(status);
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
		} catch (SecurityException e) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(Map.of("message", "Forbidden. You do not have permission to perform this action."));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("message", "An unexpected server error occurred."));
		}
	}

	@GetMapping("/institutes/by-programme/{programmeId}")
	@ResponseBody
	@Transactional(readOnly = true)
	public ResponseEntity<List<Map<String, Object>>> getInstitutesByProgramme(@PathVariable Short programmeId) {
		List<ProgrammeOffered> offerings = programmesOfferedRepository.findByProgrammeProgrammeId(programmeId,
				InstituteStatus.ACCEPTED);

		List<Map<String, Object>> responseList = offerings.stream().map(po -> {
			Institute institute = po.getInstituteDepartment().getInstitute();
			Map<String, Object> map = new HashMap<>();

			map.put("programmeOfferedId", po.getProgrammeOfferedId());
			map.put("instituteId", institute.getInstituteId());
			map.put("instituteName", institute.getInstituteName());
			map.put("shift", po.getShift() != null ? po.getShift().name() : "NA");
			map.put("shiftDisplayName", po.getShift() != null ? po.getShift().getDisplayName() : "Not Applicable");

			return map;
		}).sorted(Comparator.comparing((Map<String, Object> m) -> (String) m.get("instituteName"))
				.thenComparing(m -> (String) m.get("shiftDisplayName"))).collect(Collectors.toList());
		return ResponseEntity.ok(responseList);
	}

	@PostMapping("/programmes/preferences/save")
	@ResponseBody
	public ResponseEntity<?> saveProgrammePreferences(@Valid @RequestBody ProgrammePreferenceRequestDTO requestDTO,
			Authentication auth) {
		String applicantNo = auth.getName();
		try {
			programmePreferenceService.savePreferences(requestDTO, applicantNo);

			ApplicationStatusResponseDTO status = applicationService
					.updateProgrammeSelectionStatus(requestDTO.getApplicationId(), applicantNo);

			return ResponseEntity.ok(status);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GetMapping("/programmes/preferences/{applicationId}")
	@ResponseBody
	public ResponseEntity<?> getProgrammePreferences(@PathVariable("applicationId") Long applicationId,
			Authentication auth) {
		List<ProgrammePreferenceResponseDTO> preferences = programmePreferenceService
				.getPreferencesByApplicationId(applicationId, auth.getName());

		return ResponseEntity.ok(preferences);
	}

	@GetMapping("/programmes/by-stream/{streamId}")
	@ResponseBody
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

	@GetMapping("/programmes/by-level/{level}")
	@ResponseBody
	public ResponseEntity<List<ProgrammeResponseDTO>> getProgrammesByLevel(@PathVariable ProgrammeLevel level) {
		List<ProgrammeOffered> programmeOffered = programmesOfferedRepository.findByProgramme_ProgrammeLevel(level)
				.stream()
				.filter(po -> po.getInstituteDepartment().getInstitute().getStatus() == InstituteStatus.ACCEPTED)
				.collect(Collectors.toList());

		List<ProgrammeResponseDTO> programmes = programmeOffered.stream().map(po -> {
			Programme programme = po.getProgramme();
			ProgrammeResponseDTO dto = new ProgrammeResponseDTO();
			dto.setProgrammeId(programme.getProgrammeId());
			dto.setProgrammeName(programme.getProgrammeName());
			dto.setProgrammeLevel(programme.getProgrammeLevel());
			dto.setStreamId(programme.getStream().getStreamId());
			dto.setStreamName(programme.getStream().getStreamName());
			return dto;
		}).distinct().sorted(Comparator.comparing(ProgrammeResponseDTO::getProgrammeName)).collect(Collectors.toList());

		return ResponseEntity.ok(programmes);
	}

	@PostMapping("/fees/calculate-total")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> calculateTotalFee(@RequestBody Map<String, Long> payload,
			Authentication auth) {
		try {
			String applicantNo = auth.getName();
			Long applicationId = payload.get("applicationId");

			Applicant applicant = applicantRepository.findByApplicantNo(applicantNo).orElse(null);

			if (applicant == null) {
				return ResponseEntity.ok(Map.of("totalFee", BigDecimal.ZERO, "preferenceCount", 0));
			}

			if (applicationId == null) {
				applicationId = applicant.getLastSelectedApplicationId();
			}

			if (applicationId == null) {
				return ResponseEntity.ok(Map.of("totalFee", BigDecimal.ZERO, "preferenceCount", 0));
			}

			// We still fetch active preferences just to let the UI know how many were
			// selected
			List<ProgrammePreferenceResponseDTO> activePreferences = programmePreferenceService
					.getPreferencesByApplicationId(applicationId, applicantNo);

			long preferenceCount = activePreferences != null
					? activePreferences.stream().filter(p -> Boolean.TRUE.equals(p.getIsActive())).count()
					: 0;

			// --- NEW FLAT ADMISSION FEE LOGIC ---
			boolean isLocal = Boolean.TRUE.equals(applicant.getHasDomicileCertificate());
			String category = applicant.getCommunityCategory() != null
					? applicant.getCommunityCategory().getCategoryCode().trim().toUpperCase()
					: "GEN";

			BigDecimal totalFee;

			if (!isLocal) {
				totalFee = new BigDecimal("1000.00"); // Outside State
			} else if ("ST".equals(category) || "SC".equals(category)) {
				totalFee = new BigDecimal("200.00"); // Local SC / ST
			} else {
				totalFee = new BigDecimal("500.00"); // Local OBC / General
			}

			return ResponseEntity.ok(Map.of("totalFee", totalFee, "preferenceCount", preferenceCount, "isLocal",
					isLocal, "category", category));

		} catch (Exception e) {
			return ResponseEntity.internalServerError().body(Map.of("error", "Fee calculation failed."));
		}
	}

	@GetMapping("/streams/subjects/{streamId}")
	@ResponseBody
	public ResponseEntity<Set<Subject>> getSubjectsForStream(@PathVariable Short streamId) {
		Stream stream = streamRepository.findById(streamId)
				.orElseThrow(() -> new EntityNotFoundException("Stream not found with ID: " + streamId));
		Set<Subject> sortedSubjects = stream.getSubjects().stream()
				.sorted(Comparator.comparing(Subject::getSubjectName))
				.collect(Collectors.toCollection(LinkedHashSet::new));

		return ResponseEntity.ok(sortedSubjects);
	}

	@GetMapping("/application-status/{applicationId}")
	@ResponseBody
	public ResponseEntity<ApplicationStatusResponseDTO> getApplicationStatus(@PathVariable Long applicationId,
			Authentication auth) {
		try {
			ApplicationStatusResponseDTO status = applicationService.getApplicationStatus(applicationId,
					auth.getName());
			return ResponseEntity.ok(status);
		} catch (Exception e) {
			return ResponseEntity.internalServerError().build();
		}
	}

	@PostMapping("/eligibility/data/recalculate")
	public ResponseEntity<Void> recalculateEligibility(@RequestParam Long applicationId) {
		Application app = applicationRepository.findById(applicationId)
				.orElseThrow(() -> new RuntimeException("Application not found"));
		eligibilityCalculationService.calculateAndSaveEligibility(app);
		return ResponseEntity.ok().build();
	}

	@PostMapping("/select-active-application")
	@ResponseBody
	public ResponseEntity<?> setActiveApplication(@RequestBody Map<String, Long> payload, Authentication auth) {
		String applicantNo = auth.getName();
		Long applicationId = payload.get("applicationId");

		applicantService.setLastSelectedApplication(applicantNo, applicationId);

		return ResponseEntity.ok(Map.of("message", "Active application updated successfully."));
	}
}