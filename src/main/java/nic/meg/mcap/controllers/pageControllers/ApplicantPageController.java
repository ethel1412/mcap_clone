package nic.meg.mcap.controllers.pageControllers;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityNotFoundException;
import nic.meg.mcap.dto.request.AcademicDetailsDTO;
import nic.meg.mcap.dto.request.ApplicantAddressRequestDTO;
import nic.meg.mcap.dto.request.LatestAcademicRecordRequestDTO;
import nic.meg.mcap.dto.request.PastAcademicRecordRequestDTO;
import nic.meg.mcap.dto.request.PersonalDetailsRequestDTO;
import nic.meg.mcap.dto.request.RegistrationFormDTO;
import nic.meg.mcap.dto.response.ApplicationStatusResponseDTO;
import nic.meg.mcap.dto.response.ProgrammePreferenceResponseDTO;
import nic.meg.mcap.dto.response.SeatAllotmentResponseDTO;
import nic.meg.mcap.entities.AdmissionWindow;
import nic.meg.mcap.entities.Applicant;
import nic.meg.mcap.entities.Application;
import nic.meg.mcap.entities.CuetPaper;
import nic.meg.mcap.entities.Document;
import nic.meg.mcap.entities.EligibilityCriteria;
import nic.meg.mcap.entities.EligibilityRuleSet;
import nic.meg.mcap.entities.Programme;
import nic.meg.mcap.entities.ProgrammeOffered;
import nic.meg.mcap.entities.Qualification;
import nic.meg.mcap.entities.Schedule;
import nic.meg.mcap.entities.SeatAllotment;
import nic.meg.mcap.entities.Stream;
import nic.meg.mcap.entities.Subject;
import nic.meg.mcap.entities.SubjectRequirement;
import nic.meg.mcap.enums.ProgrammeLevel;
import nic.meg.mcap.enums.QualificationLevel;
import nic.meg.mcap.enums.SubjectType;
import nic.meg.mcap.repositories.AcademicRecordRepository;
import nic.meg.mcap.repositories.AddressRepository;
import nic.meg.mcap.repositories.AdmissionWindowRepository;
import nic.meg.mcap.repositories.ApplicantRepository;
import nic.meg.mcap.repositories.ApplicationRepository;
import nic.meg.mcap.repositories.CommunityCategoryRepository;
import nic.meg.mcap.repositories.CountryRepository;
import nic.meg.mcap.repositories.CuetPaperRepository;
import nic.meg.mcap.repositories.CuetScoreRepository;
import nic.meg.mcap.repositories.EligibilityCriteriaRepository;
import nic.meg.mcap.repositories.GenderRepository;
import nic.meg.mcap.repositories.InstituteRepository;
import nic.meg.mcap.repositories.JeeScoreRepository;
import nic.meg.mcap.repositories.MaritalStatusRepository;
import nic.meg.mcap.repositories.ProgrammeRepository;
import nic.meg.mcap.repositories.ProgrammesOfferedRepository;
import nic.meg.mcap.repositories.RelationshipRepository;
import nic.meg.mcap.repositories.ReligionRepository;
import nic.meg.mcap.repositories.ScheduleRepository;
import nic.meg.mcap.repositories.SeatAllotmentRepository;
import nic.meg.mcap.repositories.SequenceGeneratorRepository;
import nic.meg.mcap.repositories.StateRepository;
import nic.meg.mcap.repositories.StreamRepository;
import nic.meg.mcap.repositories.SubjectRepository;
import nic.meg.mcap.repositories.UserRepository;
import nic.meg.mcap.services.AcademicService;
import nic.meg.mcap.services.ApplicantService;
import nic.meg.mcap.services.ApplicationService;
import nic.meg.mcap.services.CounselingService;
import nic.meg.mcap.services.DocumentService;
import nic.meg.mcap.dto.response.InstituteSeatFeeStructureResponseDTO;
import nic.meg.mcap.services.InstituteSeatFeeService;
import nic.meg.mcap.services.InstituteRegistrationFeeService;
import nic.meg.mcap.services.PdfGenerationService;
import nic.meg.mcap.services.ProgrammePreferenceService;
import nic.meg.mcap.services.QualificationService;

@Controller
@RequestMapping("/applicants")
public class ApplicantPageController {

	private static final Logger logger = LoggerFactory.getLogger(ApplicantPageController.class);

    @Autowired
    private InstituteSeatFeeService instituteSeatFeeService;
	@Autowired
	private RelationshipRepository relationshipRepository;
	@Autowired
	private StateRepository stateRepository;
	@Autowired
	private ReligionRepository religionRepository;
	@Autowired
	private MaritalStatusRepository maritalStatusRepository;
	@Autowired
	private CountryRepository countryRepository;
	@Autowired
	private CommunityCategoryRepository communityCategoryRepository;
	@Autowired
	private GenderRepository genderRepository;
	@Autowired
	private ApplicantRepository applicantRepository;
	@Autowired
	private AddressRepository addressRepository;
	@Autowired
	private AcademicRecordRepository academicRecordRepository;
	@Autowired
	private AcademicService academicService;
	@Autowired
	private DocumentService documentService;
	@Autowired
	private ProgrammesOfferedRepository programmesOfferedRepository;
	@Autowired
	private ApplicationRepository applicationRepository;
	@Autowired
	private ApplicantService applicantService;
	@Autowired
	private AdmissionWindowRepository admissionWindowRepository;
	@Autowired
	private InstituteRepository instituteRepository;
	@Autowired
	private StreamRepository streamRepository;
	@Autowired
	private SubjectRepository subjectRepository;
	@Autowired
	private CuetScoreRepository cuetScoreRepository;
	@Autowired
	private JeeScoreRepository jeeScoreRepository;
	@Autowired
	private ProgrammePreferenceService programmePreferenceService;
	@Autowired
	private PdfGenerationService pdfGenerationService;
	@Autowired
	private QualificationService qualificationService;
	@Autowired
	private ApplicationService applicationService;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private InstituteRegistrationFeeService feeService;
	@Autowired
	private CounselingService counselingService;
	@Autowired
	private ProgrammeRepository programmeRepository;
	@Autowired
	private EligibilityCriteriaRepository eligibilityCriteriaRepository;
	@Autowired
	private SeatAllotmentRepository seatAllotmentRepository;
	@Autowired
	private CuetPaperRepository cuetPaperRepository;
	@Autowired
	private SequenceGeneratorRepository sequenceGeneratorRepository;
	@Autowired
	private ScheduleRepository scheduleRepository;

	// THE FIX: @InitBinder completely removed here as well.

	private void populateDropdowns(Model model) {
		model.addAttribute("states", stateRepository.findAll(Sort.by("stateName")));
		model.addAttribute("countries", countryRepository.findAll(Sort.by("countryName")));
		model.addAttribute("communityCategories", communityCategoryRepository.findAll(Sort.by("categoryName")));
		model.addAttribute("genders", genderRepository.findAll(Sort.by("genderName")));

		model.addAttribute("relationships", relationshipRepository.findAll());
		model.addAttribute("religions", religionRepository.findAll());
		model.addAttribute("maritalStatuses", maritalStatusRepository.findAll());
		model.addAttribute("districts", Collections.emptyList());
		model.addAttribute("blocks", Collections.emptyList());
	}

	@GetMapping("/login")
	public String showApplicantLogin(Model model, jakarta.servlet.http.HttpSession session) {

		Short admissionIdFromSession = (Short) session.getAttribute("selectedAdmissionId");

		if (admissionIdFromSession != null) {
			model.addAttribute("specificAdmissionId", admissionIdFromSession);
		} else {
			admissionWindowRepository.findByIsActive(true).stream().findFirst()
					.ifPresent(window -> model.addAttribute("defaultAdmissionId", window.getAdmissionId()));
		}
		return "login";
	}

	@GetMapping("/register")
	public String showRegistrationForm(Model model, jakarta.servlet.http.HttpSession session,
			RedirectAttributes redirectAttributes) {

		String admissionCode = (String) session.getAttribute("admissionCode");

		if (admissionCode == null) {
			redirectAttributes.addFlashAttribute("error",
					"Please select an admission window from the home page to begin registration.");
			return "redirect:/";
		}

		RegistrationFormDTO registrationFormDTO = new RegistrationFormDTO();
		registrationFormDTO.setCountryPhoneCode("+91");
		model.addAttribute("applicantDTO", registrationFormDTO);
		model.addAttribute("admissionCode", admissionCode);
		model.addAttribute("maxDob", LocalDate.now().minusYears(12));
		populateDropdowns(model);

		return "applicant-registration";
	}

	@GetMapping("/dashboard")
	public String showApplicantDashboard(Model model, Authentication auth) {
		String applicantNo = auth.getName();
		Applicant applicant = applicantRepository.findByApplicantNo(applicantNo)
				.orElseThrow(() -> new RuntimeException("Applicant not found: " + applicantNo));

		String fullName = applicant.getFirstName()
				+ (applicant.getMiddleName() != null && !applicant.getMiddleName().trim().isEmpty()
						? " " + applicant.getMiddleName()
						: "")
				+ " " + applicant.getLastName();

		model.addAttribute("applicantName", fullName.trim());
		model.addAttribute("applicantNo", applicant.getApplicantNo());

		if (applicant.getApplications() != null && !applicant.getApplications().isEmpty()) {
			Long activeAppId = applicant.getLastSelectedApplicationId();
			Application activeApp = applicant.getApplications().stream()
					.filter(app -> app.getApplicationId().equals(activeAppId)).findFirst()
					.orElse(applicant.getApplications().get(0));

			model.addAttribute("activeApplicationId", activeApp.getApplicationId());
		} else {
			model.addAttribute("activeApplicationId", "");
		}

		return "applicant/dashboard";
	}

	@GetMapping("/fragments/dashboard")
	public String getUnifiedDashboardFragment(Model model, Authentication auth) {
		String applicantNo = auth.getName();
		Applicant applicant = applicantRepository.findByApplicantNo(applicantNo)
				.orElseThrow(() -> new RuntimeException("Applicant not found for " + applicantNo));

		String fullName = applicant.getFirstName()
				+ (applicant.getMiddleName() != null && !applicant.getMiddleName().trim().isEmpty()
						? " " + applicant.getMiddleName()
						: "")
				+ " " + applicant.getLastName();
		model.addAttribute("applicantName", fullName.trim());

		if (applicant.getApplications() == null || applicant.getApplications().isEmpty()) {
			model.addAttribute("currentStep", 1);
			return "applicant/fragments/dashboard";
		}

		Long activeAppId = applicant.getLastSelectedApplicationId();
		Application activeApp = applicant.getApplications().stream()
				.filter(app -> app.getApplicationId().equals(activeAppId)).findFirst()
				.orElse(applicant.getApplications().get(0));

		model.addAttribute("applicationId", activeApp.getApplicationId());
		model.addAttribute("admissionWindowId", activeApp.getAdmissionWindow().getAdmissionId());

		int currentStep = 1;
		if (activeApp.isPersonalDetailsComplete())
			currentStep = 2;
		if (activeApp.isAcademicDetailsComplete())
			currentStep = 3;
		if (activeApp.isProgrammeSelectionComplete())
			currentStep = 4;
		if (activeApp.isDocumentsFinalized())
			currentStep = 5;
		if (activeApp.isPaymentComplete())
			currentStep = 6;

		model.addAttribute("currentStep", currentStep);

		boolean hasAllotment = false;
		if (currentStep == 6) {
			try {
				SeatAllotmentResponseDTO allotment = counselingService.getSeatAllotmentForWindow(applicantNo,
						activeApp.getAdmissionWindow().getAdmissionId());
				if (allotment != null) {
					hasAllotment = true;
				}
			} catch (Exception e) {
				hasAllotment = false;
			}
		}
		model.addAttribute("hasAllotment", hasAllotment);

		return "applicant/fragments/dashboard";
	}

	@GetMapping("/fragments/personal-details")
	public String getPersonalDetailsFragment(@RequestParam("applicationId") Long applicationId, Model model,
			Authentication auth) {
		String applicantNo = auth.getName();
		ApplicationStatusResponseDTO status = applicationService.getApplicationStatus(applicationId, applicantNo);
		model.addAttribute("status", status);

		Applicant applicant = applicantRepository.findByApplicantNo(applicantNo)
				.orElseThrow(() -> new RuntimeException("Applicant not found"));

		PersonalDetailsRequestDTO dto = new PersonalDetailsRequestDTO();

		dto.setFirstName(applicant.getFirstName());
		dto.setMiddleName(applicant.getMiddleName());
		dto.setLastName(applicant.getLastName());
		dto.setDateOfBirth(applicant.getDateOfBirth());
		dto.setCountryPhoneCode(applicant.getCountryPhoneCode());
		dto.setPhoneNumber(applicant.getPhoneNumber());
		dto.setEmail(applicant.getEmail());

		if (applicant.getGender() != null) {
			dto.setGenderCode(applicant.getGender().getGenderCode());
		}

		if (applicant.getCommunityCategory() != null) {
			dto.setCommunityCategoryCode(applicant.getCommunityCategory().getCategoryCode().trim());
		} else {
			dto.setCommunityCategoryCode("ST");
		}

		if (applicant.getMaritalStatus() != null) {
			dto.setMaritalStatusCode(applicant.getMaritalStatus().getStatusCode().trim());
		} else {
			dto.setMaritalStatusCode("U");
		}

		if (applicant.getReligion() != null) {
			dto.setReligionCode(applicant.getReligion().getReligionCode().trim());
		} else {
			dto.setReligionCode("CHR");
		}

		if (applicant.getCountryOfOrigin() != null) {
			dto.setCountryCode(applicant.getCountryOfOrigin().getCountryCode());
		} else {
			dto.setCountryCode((short) 356);
		}

		dto.setFatherName(applicant.getFatherName());
		dto.setMotherName(applicant.getMotherName());
		dto.setGuardianName(applicant.getGuardianName());
		dto.setGuardianPrefix(applicant.getGuardianPrefix());
		if (applicant.getGuardianRelationship() != null) {
			dto.setGuardianRelationshipCode(applicant.getGuardianRelationship().getRelationshipCode());
		}

		dto.setHasDomicileCertificate(applicant.getHasDomicileCertificate());
		dto.setIsDifferentlyAbled(applicant.getIsDifferentlyAbled());
		dto.setHasNccCertificate(applicant.getHasNccCertificate());
		dto.setHasNssCertificate(applicant.getHasNssCertificate());
		dto.setHasBackwardAreaCertificate(applicant.getHasBackwardAreaCertificate());
		dto.setHasAnyOtherRelevantCertificate(applicant.getHasAnyOtherRelevantCertificate());

		ApplicantAddressRequestDTO permanentAddress = applicantService.getApplicantAddress(applicant.getApplicantId(),
				"Permanent");
		ApplicantAddressRequestDTO communicationAddress = applicantService
				.getApplicantAddress(applicant.getApplicantId(), "Communication");
		dto.setPermanentAddress(permanentAddress);
		dto.setCommunicationAddress(communicationAddress);

		model.addAttribute("applicant", applicant);
		model.addAttribute("personalDetailsDTO", dto);
		model.addAttribute("applicationId", applicationId);

		populateDropdowns(model);
		model.addAttribute("prefixes", Arrays.asList("Shri", "Smt"));

		return "applicant/fragments/personal-details";
	}

	@GetMapping("/fragments/academic-details")
	public String getAcademicDetailsFragment(Model model, Authentication auth,
			@RequestParam("applicationId") Long applicationId) {
		String applicantNo = auth.getName();

		ApplicationStatusResponseDTO status = applicationService.getApplicationStatus(applicationId, applicantNo);
		model.addAttribute("status", status);

		Application currentApplication = applicationRepository.findById(applicationId)
				.orElseThrow(() -> new RuntimeException("Application not found"));
		if (!currentApplication.getApplicant().getApplicantNo().equals(applicantNo)) {
			throw new SecurityException("Unauthorized access");
		}

		ProgrammeLevel programmeLevel = currentApplication.getAdmissionWindow().getProgrammeLevel();
		AcademicDetailsDTO formDTO = academicService.getAcademicDetails(applicantNo);

		formDTO.setApplicationId(applicationId);

		List<Qualification> allQualifications = qualificationService.getAllActiveQualifications();
		List<Qualification> latestQualificationOptions = new ArrayList<>();
		List<Qualification> pastQualificationOptions = new ArrayList<>();

		if (programmeLevel == ProgrammeLevel.PG) {
			for (Qualification q : allQualifications) {
				if (q.getLevel() == QualificationLevel.UG || q.getLevel() == QualificationLevel.PG_DIPLOMA
						|| q.getName().toLowerCase().contains("class xii")) {
					latestQualificationOptions.add(q);
				} else if (q.getLevel() == QualificationLevel.SCHOOL || q.getLevel() == QualificationLevel.DIPLOMA) {
					pastQualificationOptions.add(q);
				}
			}
		} else {
			for (Qualification q : allQualifications) {
				String qualNameLower = q.getName().toLowerCase();
				if (qualNameLower.contains("class xii") || qualNameLower.contains("class xi")
						|| q.getLevel() == QualificationLevel.DIPLOMA) {
					latestQualificationOptions.add(q);
				}
				if (qualNameLower.contains("class x") || qualNameLower.contains("class ix")
						|| qualNameLower.contains("class viii") || qualNameLower.contains("class vii")) {
					pastQualificationOptions.add(q);
				}
			}
		}

		Qualification otherQualification = new Qualification();
		otherQualification.setName("Other");
		latestQualificationOptions.add(otherQualification);
		pastQualificationOptions.add(otherQualification);

		latestQualificationOptions.sort(Comparator.comparing(Qualification::getName));
		pastQualificationOptions.sort(Comparator.comparing(Qualification::getName));

		if (formDTO.getLatestRecords().isEmpty()) {
			formDTO.getLatestRecords().add(new LatestAcademicRecordRequestDTO());
		}
		if (formDTO.getPastRecords().isEmpty()) {
			formDTO.getPastRecords().add(new PastAcademicRecordRequestDTO());
		}

		int currentYear = Year.now().getValue();
		List<Integer> passingYears = IntStream.rangeClosed(currentYear - 50, currentYear).boxed()
				.sorted(Comparator.reverseOrder()).collect(Collectors.toList());
		List<Stream> allStreams = streamRepository.findAll(Sort.by("streamName"));
		List<Subject> allSubjects = subjectRepository.findBySubjectTypeOrderBySubjectName(SubjectType.GENERAL);
		String allSubjectsJson = "[]";
		try {
			allSubjectsJson = new ObjectMapper().writeValueAsString(allSubjects);
		} catch (JsonProcessingException e) {
			logger.info("Error converting subjects to JSON", e);
		}

		Stream programmeStream = currentApplication.getAdmissionWindow().getStream();

		List<Programme> programmesInStream;

		if (programmeStream != null) {
			programmesInStream = programmeRepository.findByStreamStreamId(programmeStream.getStreamId());
		} else {
			List<Short> defaultStreamIds = List.of((short) 101, (short) 102, (short) 103);

			programmesInStream = programmeRepository.findByStreamStreamIdIn(defaultStreamIds);
		}

		Set<String> aggregatedSubjectRequirements = new LinkedHashSet<>();
		String streamName = (programmeStream != null) ? programmeStream.getStreamName() : "Multiple Streams";

		aggregatedSubjectRequirements
				.add(String.format("The following requirements apply to programmes in the **%s** stream:", streamName));

		Short windowId = currentApplication.getAdmissionWindow().getAdmissionId();

		for (Programme p : programmesInStream) {

			Optional<EligibilityCriteria> criteriaOpt = eligibilityCriteriaRepository
					.findByAdmissionWindowAdmissionCodeAndProgrammeProgrammeId(
							currentApplication.getAdmissionWindow().getAdmissionCode(), p.getProgrammeId());

			if (criteriaOpt.isEmpty())
				continue;

			EligibilityCriteria criteria = criteriaOpt.get();
			if (criteria == null)
				continue;

			String programmeName = p.getProgrammeName();
			String baseQualName = criteria.getBaseQualification() != null ? criteria.getBaseQualification().getName()
					: "Latest Qualification";

			if (criteria.getMinOverallPercentage() != null && criteria.getMinOverallPercentage() > 0) {
				String streamName1 = (programmeStream != null) ? programmeStream.getStreamName() : "Multiple Streams";

				aggregatedSubjectRequirements.add(String
						.format("The following requirements apply to programmes in the **%s** stream:", streamName1));
			}

			List<EligibilityRuleSet> ruleSets = criteria.getRuleSets() != null ? criteria.getRuleSets() : List.of();
			if (ruleSets.isEmpty())
				continue;

			int ruleIndex = 1;
			for (EligibilityRuleSet ruleSet : ruleSets) {
				if (ruleSet == null)
					continue;

				String ruleTitle = (ruleSet.getDescription() != null && !ruleSet.getDescription().isBlank())
						? ruleSet.getDescription().trim()
						: ("Rule " + ruleIndex);

				List<SubjectRequirement> reqs = ruleSet.getSubjectRequirements() != null
						? ruleSet.getSubjectRequirements()
						: List.of();

				for (SubjectRequirement subjectReq : reqs) {
					if (subjectReq == null)
						continue;

					String scoreSourceDisplay = subjectReq.getScoreSource() != null
							? subjectReq.getScoreSource().name().replace("_", " ")
							: "UNKNOWN";

					String calculationTypeDisplay = subjectReq.getCalculationType() != null
							? subjectReq.getCalculationType().name().replace("_", " ")
							: "UNKNOWN";

					String subjectsDisplay = subjectReq.getSubjectNames() == null ? ""
							: Arrays.stream(subjectReq.getSubjectNames()).filter(Objects::nonNull).map(String::trim)
									.filter(s -> !s.isBlank()).collect(Collectors.joining(", "));

					aggregatedSubjectRequirements.add(String.format(
							"- For **%s** (%s): Minimum <strong>%.1f</strong> with <strong>%s</strong> in <strong>%s</strong> (Score source: %s).",
							programmeName, ruleTitle, subjectReq.getMinScore(), calculationTypeDisplay.toLowerCase(),
							subjectsDisplay, scoreSourceDisplay));
				}

				ruleIndex++;
			}
		}

		if (aggregatedSubjectRequirements.size() <= 1) {
			aggregatedSubjectRequirements.clear();
		}

		List<CuetPaper> activeCuetPapers = cuetPaperRepository
				.findByProgrammeLevelAndIsActiveOrderBySpecAscSortOrderAscPaperNameAsc(programmeLevel, true);

		try {
			model.addAttribute("cuetPapersJson", new ObjectMapper().writeValueAsString(activeCuetPapers));
		} catch (JsonProcessingException e) {
			model.addAttribute("cuetPapersJson", "[]");
		}

		model.addAttribute("streamRequirements", new ArrayList<>(aggregatedSubjectRequirements));

		model.addAttribute("academicDetailsDTO", formDTO);
		model.addAttribute("latestQualificationOptions", latestQualificationOptions);
		model.addAttribute("pastQualificationOptions", pastQualificationOptions);
		model.addAttribute("programmeLevel", programmeLevel);
		model.addAttribute("passingYears", passingYears);
		model.addAttribute("allStreams", allStreams);
		model.addAttribute("allSubjects", allSubjects);
		model.addAttribute("allSubjectsJson", allSubjectsJson);

		return "applicant/fragments/academic-details";
	}

	@GetMapping("/fragments/programme-selection")
	public String getProgrammeSelectionPage(@RequestParam("applicationId") Long applicationId, Model model,
			Authentication auth) {

		String applicantNo = auth.getName();
		ApplicationStatusResponseDTO status = applicationService.getApplicationStatus(applicationId, applicantNo);
		model.addAttribute("status", status);

		Application application = applicationRepository.findByIdWithDetails(applicationId)
				.orElseThrow(() -> new RuntimeException("Application not found"));

		if (!application.getApplicant().getApplicantNo().equals(applicantNo)) {
			throw new SecurityException("Unauthorized access to application");
		}

		AdmissionWindow admissionWindow = application.getAdmissionWindow();

		Stream windowStream = admissionWindow.getStream();
		boolean isAllStreams = (windowStream == null);
		ProgrammeLevel requiredLevel = admissionWindow.getProgrammeLevel();

		model.addAttribute("studiedSubjectsJson", "[]");

		List<ProgrammePreferenceResponseDTO> existingPreferences = programmePreferenceService
				.getPreferencesByApplicationId(applicationId, applicantNo);

		if (existingPreferences != null && !existingPreferences.isEmpty()) {
			model.addAttribute("preferences", existingPreferences);
		} else {
			List<ProgrammePreferenceResponseDTO> blankPreferences = new ArrayList<>();
			model.addAttribute("preferences", blankPreferences);
		}

		List<Stream> activeStreams;
		if (isAllStreams) {
			activeStreams = programmesOfferedRepository.findAllByActiveAndAcceptedInstitutes().stream()
					.filter(po -> po.getProgramme().getProgrammeLevel() == requiredLevel)
					.map(po -> po.getProgramme().getStream()).filter(Objects::nonNull).distinct()
					.sorted(Comparator.comparing(Stream::getStreamName))
					.collect(Collectors.toCollection(ArrayList::new));
		} else {
			activeStreams = new ArrayList<>();
			activeStreams.add(windowStream);
		}

		model.addAttribute("allStreams", activeStreams);
		model.addAttribute("applicationId", applicationId);
		model.addAttribute("isAllStreams", isAllStreams);
		model.addAttribute("programmeLevel", requiredLevel.name());
		model.addAttribute("streamId", isAllStreams ? null : windowStream.getStreamId());
		model.addAttribute("streamName", isAllStreams ? "All Streams" : windowStream.getStreamName());
		model.addAttribute("session", admissionWindow.getSession());

		return "applicant/fragments/programme-selection";
	}

	@GetMapping("/fragments/document-upload")
	public String getDocumentUploadFragment(Model model, Authentication auth,
			@RequestParam("applicationId") Long applicationId) {
		String applicantNo = auth.getName();

		ApplicationStatusResponseDTO status = applicationService.getApplicationStatus(applicationId, applicantNo);
		model.addAttribute("status", status);

		Map<String, String> requiredDocTypes = documentService.getRequiredDocumentTypes(applicantNo, applicationId);

		List<Document> uploadedDocuments = documentService.getUploadedDocuments(applicantNo);

		Map<String, Document> uploadedDocsMap = uploadedDocuments.stream()
				.collect(Collectors.toMap(Document::getDocumentType, doc -> doc, (doc1, doc2) -> doc1));

		model.addAttribute("requiredDocTypes", requiredDocTypes);
		model.addAttribute("uploadedDocsMap", uploadedDocsMap);
		model.addAttribute("isDocumentsFinalized", status.isDocumentsUploadComplete());

		return "applicant/fragments/document-upload";
	}

	@GetMapping("/application/get-filename/{applicationId}")
	@ResponseBody
	public ResponseEntity<Map<String, String>> getApplicationFilename(@PathVariable Long applicationId,
			Authentication auth) {
		Application application = applicationRepository.findById(applicationId)
				.filter(app -> app.getApplicant().getApplicantNo().equals(auth.getName()))
				.orElseThrow(() -> new SecurityException("Application not found or unauthorized."));

		String streamCode = "APP";
		Stream stream = application.getAdmissionWindow().getStream();
		if (stream != null && stream.getStreamName() != null && !stream.getStreamName().isEmpty()) {
			int length = Math.min(stream.getStreamName().length(), 4);
			streamCode = stream.getStreamName().substring(0, length).toUpperCase();
		}

		String session = "SESSION";
		if (application.getAdmissionWindow().getSession() != null) {
			session = application.getAdmissionWindow().getSession().replace("/", "-");
		}

		String appNo = "00000";
		String fullApplicationNo = application.getApplicationNo();
		if (fullApplicationNo != null && fullApplicationNo.length() > 5) {
			appNo = fullApplicationNo.substring(fullApplicationNo.length() - 5);
		}

		String filename = String.format("Application-%s-%s-%s.pdf", streamCode, session, appNo);

		return ResponseEntity.ok(Map.of("filename", filename));

	}

	@GetMapping("/application/print-pdf/{applicationId}")
	public ResponseEntity<byte[]> printApplication(@PathVariable Long applicationId, Authentication auth)
			throws IOException {
		String applicantNo = auth.getName();
		byte[] pdfBytes = pdfGenerationService.generateApplicationPdf(applicationId, applicantNo);

		return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
				.header("Content-Disposition", "inline; filename=\"application-" + applicationId + ".pdf\"")
				.body(pdfBytes);
	}

	@GetMapping("/fragments/counseling/result")
	public String getSeatAllotmentResultFragment(@RequestParam("admissionWindowId") Short admissionWindowId,
			Model model, Authentication auth) throws AccessDeniedException {
		String applicantNo = auth.getName();
		try {
			// Verify the applicant actually has an application for this window (ownership
			// check)
			Application application = applicationRepository
					.findByAdmissionWindow_AdmissionIdAndApplicationStatus(admissionWindowId, "SUBMITTED").stream()
					.filter(app -> app.getApplicant().getApplicantNo().equals(applicantNo)).findFirst()
					.orElseThrow(() -> new AccessDeniedException("No submitted application found for this window."));

			SeatAllotmentResponseDTO allotmentDetails = counselingService.getSeatAllotmentForWindow(applicantNo,
					admissionWindowId);

			model.addAttribute("allotment", allotmentDetails);

			if (allotmentDetails != null && "ACCEPTED".equalsIgnoreCase(allotmentDetails.getStatus())
					&& allotmentDetails.getAllotmentId() != null) {

				seatAllotmentRepository.findById(allotmentDetails.getAllotmentId())
						.map(SeatAllotment::getProgrammeOffered).map(ProgrammeOffered::getProgrammeOfferedId)
						.ifPresent(id -> model.addAttribute("programmeOfferedId", id));
			}

			return "applicant/fragments/seat-allotment-result";

		} catch (EntityNotFoundException e) {
			model.addAttribute("errorMessage", e.getMessage());
			return "applicant/fragments/counseling-error";
		}
	}

	@GetMapping("/select-subjects")
	public String getSelectSubjectsPage(@RequestParam Long allotmentId, Authentication auth, Model model) {
		SeatAllotment allotment = seatAllotmentRepository.findById(allotmentId)
				.orElseThrow(() -> new EntityNotFoundException("Allotment not found."));

		if (!allotment.getApplicant().getApplicantNo().equals(auth.getName())) {
			throw new SecurityException("Unauthorized access to allotment.");
		}

		model.addAttribute("seatAllotmentId", allotment.getId());
		model.addAttribute("programmeOfferedId", allotment.getProgrammeOffered().getProgrammeOfferedId());
		model.addAttribute("programmeName", allotment.getProgrammeOffered().getProgramme().getProgrammeName());
		model.addAttribute("instituteName",
				allotment.getProgrammeOffered().getInstituteDepartment().getInstitute().getInstituteName());

		return "applicant/fragments/select-subjects";
	}

	@GetMapping("/fragments/counseling/payment-page")
	public String getCounselingPaymentFragment(@RequestParam("allotmentId") Long allotmentId, Model model,
			Authentication auth) {
		String applicantNo = auth.getName();

		try {
			SeatAllotmentResponseDTO allotmentDTO = counselingService.getSeatAllotmentDetailsById(applicantNo,
					allotmentId);

			// Allow ACCEPTED and SLIDE_UP — both require payment
			boolean statusOk = allotmentDTO != null && ("ACCEPTED".equalsIgnoreCase(allotmentDTO.getStatus())
					|| "SLIDE_UP".equalsIgnoreCase(allotmentDTO.getStatus()));

			if (!statusOk) {
				return "redirect:/applicants/dashboard";
			}

			// Get deadline from the Seat Acceptance schedule step for this round/phase
			SeatAllotment rawAllotment = seatAllotmentRepository.findById(allotmentId).orElseThrow();
			Short windowId = rawAllotment.getAdmissionWindow().getAdmissionId();
			String roundType = rawAllotment.getRoundType();
			Integer phaseNo = rawAllotment.getPhaseNo();

			LocalDateTime paymentDeadline = scheduleRepository.findSeatAcceptanceStep(windowId, roundType, phaseNo)
					.map(Schedule::getEndDate).orElse(LocalDateTime.now().plusDays(3));

			model.addAttribute("allotment", allotmentDTO);
			model.addAttribute("acceptanceFee", "1000.00");
			model.addAttribute("acceptanceFeeAmount", new java.math.BigDecimal("1000.00"));
			model.addAttribute("allotmentId", allotmentId);
			model.addAttribute("paymentDeadline", paymentDeadline);
			model.addAttribute("isSlideUp", "SLIDE_UP".equalsIgnoreCase(allotmentDTO.getStatus()));

            // Resolve seat acceptance fee dynamically from institute's configured fee structure
            Integer programmeOfferedId = rawAllotment.getProgrammeOffered() != null
                    ? rawAllotment.getProgrammeOffered().getProgrammeOfferedId()
                    : null;

            InstituteSeatFeeStructureResponseDTO feeStructureDTO = null;
            if (programmeOfferedId != null) {
                feeStructureDTO = instituteSeatFeeService.resolveAcceptanceFeeStructure(programmeOfferedId);
            }

            if (feeStructureDTO != null) {
                model.addAttribute("feeStructure", feeStructureDTO);
                model.addAttribute("acceptanceFee", feeStructureDTO.getTotalAmount().toPlainString());
                model.addAttribute("acceptanceFeeAmount", feeStructureDTO.getTotalAmount());
            } else {
                // No fee structure configured; block payment
                model.addAttribute("feeStructure", null);
                model.addAttribute("acceptanceFee", null);
                model.addAttribute("acceptanceFeeAmount", null);
            }

            return "applicant/fragments/counseling-payment";

		} catch (EntityNotFoundException e) {
			return "redirect:/applicants/dashboard";
		}
	}
}