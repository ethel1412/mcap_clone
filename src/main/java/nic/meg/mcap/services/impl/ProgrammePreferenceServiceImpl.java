package nic.meg.mcap.services.impl;

import nic.meg.mcap.dto.response.PreferenceApplicantDTO;
import nic.meg.mcap.enums.ApplicantType;
import nic.meg.mcap.enums.ProgrammeLevel;
import nic.meg.mcap.services.AcademicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;

import nic.meg.mcap.dto.request.ProgrammePreferenceRequestDTO;
import nic.meg.mcap.dto.response.ProgrammePreferenceResponseDTO;
import nic.meg.mcap.entities.*;
import nic.meg.mcap.repositories.*;
import nic.meg.mcap.services.ProgrammePreferenceService;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProgrammePreferenceServiceImpl implements ProgrammePreferenceService {

    @Autowired
    private ProgrammePreferenceRepository programmePreferenceRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private ProgrammesOfferedRepository programmesOfferedRepository;

    @Autowired
    private EligibilityResultRepository eligibilityResultRepository;

    @Autowired
    private AcademicService academicService;

    @Autowired
    private AdmissionWindowRepository admissionWindowRepository;

    @Override
    @Transactional
    public List<ProgrammePreferenceResponseDTO> savePreferences(ProgrammePreferenceRequestDTO requestDTO, String applicantNo) {
        // 1. Fetch Application and Verify Ownership
        Application application = applicationRepository.findById(requestDTO.getApplicationId())
                .orElseThrow(() -> new EntityNotFoundException("Application not found"));

        if (!application.getApplicant().getApplicantNo().equals(applicantNo)) {
            throw new SecurityException("Unauthorized access");
        }

        // 2. Determine if NEP rules apply — both UG and FYUG are NEP programmes
        //    and must enforce the MDC subject conflict rule (applicants cannot pick
        //    an MDC subject they already studied in Class XII).
        ProgrammeLevel level = application.getAdmissionWindow().getProgrammeLevel();
        boolean isNep = level == ProgrammeLevel.UG
                || level == ProgrammeLevel.FYUG;

        // 3. Get the "blacklist" of subjects (only if NEP applies)
        List<String> studiedSubjects = isNep ? academicService.getStudiedSubjectNames(applicantNo) : java.util.Collections.emptyList();

        // 4. Clear old preferences
        deletePreferencesByApplicationId(requestDTO.getApplicationId());

        // 5. Loop through each PreferenceItemDTO (each "Combined Block") from the request
        for (ProgrammePreferenceRequestDTO.PreferenceItemDTO item : requestDTO.getPreferences()) {

            // FETCH PROGRAMME OFFERED
            ProgrammeOffered po = programmesOfferedRepository.findById(item.getProgrammeOfferedId())
                    .orElseThrow(() -> new EntityNotFoundException("Programme Offered not found"));

            // --- NEP VALIDATION (Applied PER ITEM) ---
            if (isNep) {
                if (item.getMdcCoursePreferences() != null) {
                    for (String mdcPref : item.getMdcCoursePreferences()) {
                        if (mdcPref != null && !mdcPref.isEmpty() && studiedSubjects.contains(mdcPref)) {
                            throw new IllegalArgumentException("NEP Policy Violation: MDC choice '" + mdcPref
                                    + "' for Programme '" + po.getProgramme().getProgrammeName()
                                    + "' conflicts with subjects studied in Class 12.");
                        }
                    }
                }
            }

            // --- CREATE AND POPULATE THE ENTITY ---
            ApplicantProgrammePreference preference = new ApplicantProgrammePreference();
            preference.setApplication(application);
            preference.setProgrammeOffered(po); // Links to Institute, Programme, AND Shift!
            preference.setPreferenceOrder(item.getPreferenceOrder());
            preference.setIsActive(true);

            programmePreferenceRepository.save(preference);
        }

        return getPreferencesByApplicationId(requestDTO.getApplicationId(), applicantNo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProgrammePreferenceResponseDTO> getPreferencesByApplicationId(Long applicationId, String applicantNo) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new EntityNotFoundException("Application not found"));

        if (!application.getApplicant().getApplicantNo().equals(applicantNo)) {
            throw new SecurityException("Unauthorized access");
        }

        List<ApplicantProgrammePreference> entities = programmePreferenceRepository
                .findByApplicationApplicationIdOrderByPreferenceOrderAsc(applicationId);

        return entities.stream()
                .map(pref -> {
                    ProgrammeOffered po = pref.getProgrammeOffered();

                    ProgrammePreferenceResponseDTO dto = new ProgrammePreferenceResponseDTO();
                    dto.setId(pref.getId());
                    dto.setApplicationId(applicationId);
                    dto.setProgrammeOfferedId(po.getProgrammeOfferedId());
                    dto.setPreferenceOrder(pref.getPreferenceOrder());
                    dto.setIsActive(pref.getIsActive());

                    // Map flattened Programme & Stream
                    if (po.getProgramme() != null) {
                        dto.setProgrammeId(po.getProgramme().getProgrammeId());
                        dto.setProgrammeName(po.getProgramme().getProgrammeName());

                        if (po.getProgramme().getStream() != null) {
                            dto.setStreamId(po.getProgramme().getStream().getStreamId());
                            dto.setStreamName(po.getProgramme().getStream().getStreamName());
                        }
                    }

                    // Map flattened Institute
                    if (po.getInstituteDepartment() != null && po.getInstituteDepartment().getInstitute() != null) {
                        dto.setInstituteId(po.getInstituteDepartment().getInstitute().getInstituteId());
                        dto.setInstituteName(po.getInstituteDepartment().getInstitute().getInstituteName());
                    }

                    // Map Shift
                    dto.setShift(po.getShift() != null ? po.getShift().name() : "NA");
                    dto.setShiftDisplayName(po.getShift() != null ? po.getShift().getDisplayName() : "Not Applicable");

                    return dto;
                }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deletePreferencesByApplicationId(Long applicationId) {
        programmePreferenceRepository.deleteByApplicationApplicationId(applicationId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasPreferences(Long applicationId, String applicantNo) {
        applicationRepository.findById(applicationId)
                .filter(app -> app.getApplicant().getApplicantNo().equals(applicantNo))
                .orElseThrow(() -> new SecurityException("Unauthorized access or application not found."));

        return programmePreferenceRepository.existsByApplicationApplicationId(applicationId);
    }

    @Transactional(readOnly = true)
    @Override
    public List<PreferenceApplicantDTO> getApplicantsWhoPreferredProgramme(String admissionWindowCode, Short programmeId) {
        // Look up by code
        AdmissionWindow window = admissionWindowRepository.findByAdmissionCode(admissionWindowCode)
                .orElseThrow(() -> new EntityNotFoundException("Admission Window not found: " + admissionWindowCode));
        Short windowId = window.getAdmissionId();

        // Updated to use the new base programme query from the repository
        List<ApplicantProgrammePreference> prefs =
                programmePreferenceRepository.findAllPreferencesForBaseProgramme(windowId, programmeId);

        return prefs.stream().map(pref -> {
            Application app = pref.getApplication();
            Applicant applicant = app.getApplicant();
            ProgrammeOffered po = pref.getProgrammeOffered();

            String fullName = applicant.getFirstName() + " " +
                    (applicant.getMiddleName() != null ? applicant.getMiddleName() + " " : "") +
                    applicant.getLastName();

            EligibilityResult eligibility = eligibilityResultRepository
                    .findByApplication(app)
                    .orElse(null);

            return PreferenceApplicantDTO.builder()
                    .applicationId(app.getApplicationId())
                    .applicationNo(app.getApplicationNo())
                    .applicantName(fullName)
                    .preferenceOrder(pref.getPreferenceOrder())
                    .programmeName(po.getProgramme().getProgrammeName()) // Fetched via ProgrammeOffered
                    .instituteName(po.getInstituteDepartment().getInstitute().getInstituteName()) // Fetched via ProgrammeOffered
                    .isEligible(eligibility != null && eligibility.getIsEligible())
                    .build();
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<PreferenceApplicantDTO> getApplicantsForProgramme(
            String admissionWindowCode, int programmeId, ApplicantType applicantType) {

        // Look up by code
        AdmissionWindow window = admissionWindowRepository.findByAdmissionCode(admissionWindowCode)
                .orElseThrow(() -> new EntityNotFoundException("Admission Window not found: " + admissionWindowCode));
        Short admissionWindowId = window.getAdmissionId();

        List<EligibilityResult> results =
                eligibilityResultRepository
                        .findPreferredByProgrammeAndWindowAndApplicantType(
                                admissionWindowId, programmeId, applicantType);

        return results.stream().map(er -> {
            Application app = er.getApplication();
            Applicant applicant = app.getApplicant();

            // Updated filter to traverse through ProgrammeOffered
            ApplicantProgrammePreference pref = app.getApplicantProgrammePreferences().stream()
                    .filter(p -> p.getProgrammeOffered().getProgramme().getProgrammeId() == programmeId)
                    .findFirst()
                    .orElse(null);

            String fullName =
                    (applicant.getFirstName() != null ? applicant.getFirstName() : "") +
                            (applicant.getMiddleName() != null && !applicant.getMiddleName().isBlank()
                                    ? " " + applicant.getMiddleName() : "") +
                            (applicant.getLastName() != null ? " " + applicant.getLastName() : "");

            return PreferenceApplicantDTO.builder()
                    .applicationId(app.getApplicationId())
                    .applicationNo(app.getApplicationNo())
                    .applicantName(fullName.trim())
                    .preferenceOrder(pref != null ? pref.getPreferenceOrder() : null)
                    .programmeName(er.getProgramme().getProgrammeName())
                    .instituteName(null)
                    .isEligible(Boolean.TRUE.equals(er.getIsEligible()))
                    .build();
        }).collect(Collectors.toList());
    }
}