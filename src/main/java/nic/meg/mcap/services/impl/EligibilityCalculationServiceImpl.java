package nic.meg.mcap.services.impl;

import lombok.RequiredArgsConstructor;
import nic.meg.mcap.dto.response.EligibilityListRowDTO;
import nic.meg.mcap.dto.response.EligibilityResultResponseDTO;
import nic.meg.mcap.entities.*;
import nic.meg.mcap.enums.ApplicantType;
import nic.meg.mcap.enums.CalculationType;
import nic.meg.mcap.enums.ScoreSource;
import nic.meg.mcap.repositories.AcademicRecordRepository;
import nic.meg.mcap.repositories.ApplicationRepository;
import nic.meg.mcap.repositories.EligibilityCriteriaRepository;
import nic.meg.mcap.repositories.EligibilityResultRepository;
import nic.meg.mcap.repositories.AdmissionWindowRepository; // ADDED
import nic.meg.mcap.services.EligibilityCalculationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@RequiredArgsConstructor
public class EligibilityCalculationServiceImpl implements EligibilityCalculationService {

    private final AcademicRecordRepository academicRecordRepo;
    private final EligibilityCriteriaRepository criteriaRepo;
    private final EligibilityResultRepository resultRepo;
    private final ApplicationRepository applicationRepository;

    // CHANGED: Added AdmissionWindowRepository to perform the code lookup
    private final AdmissionWindowRepository admissionWindowRepository;

    @Override
    @Transactional
    public void calculateAndSaveEligibility(Application applicationInput) {
        Application application = applicationRepository.findById(applicationInput.getApplicationId())
                .orElseThrow(() -> new RuntimeException("Application not found"));

        Applicant applicant = application.getApplicant();

        resultRepo.deleteByApplication_ApplicationId(application.getApplicationId());

        // FIX: Extract unique BASE programmes from the new ProgrammeOffered structure
        // This ensures we only calculate eligibility once per base programme, even if they applied to multiple shifts!
        Set<Programme> uniqueProgrammes = new HashSet<>();
        if (application.getApplicantProgrammePreferences() != null) {
            for (ApplicantProgrammePreference pref : application.getApplicantProgrammePreferences()) {
                if (pref.getProgrammeOffered() != null && pref.getProgrammeOffered().getProgramme() != null) {
                    uniqueProgrammes.add(pref.getProgrammeOffered().getProgramme());
                }
            }
        }

        // Run the calculation for the unique base programmes
        for (Programme programme : uniqueProgrammes) {
            checkEligibilityForProgramme(application, applicant, programme);
        }

        resultRepo.flush();
    }

    private void checkEligibilityForProgramme(Application application, Applicant applicant, Programme programme) {
        // CHANGED: Get the code instead of the ID
        String admissionCode = application.getAdmissionWindow().getAdmissionCode();
        Short programmeId = programme.getProgrammeId();

        // CHANGED: Use the newly refactored repository method
        EligibilityCriteria criteria = criteriaRepo
                .findByAdmissionWindowAdmissionCodeAndProgrammeProgrammeId(admissionCode, programmeId)
                .orElse(null);

        if (criteria == null) {
            saveResult(application, programme, false, "Eligibility Criteria not configured.");
            return;
        }

        double relaxation = resolveRelaxation(criteria, applicant);

        Qualification requiredQual = criteria.getBaseQualification();
        if (requiredQual == null) {
            saveResult(application, programme, false, "Base Qualification not set in criteria.");
            return;
        }

        AcademicRecord qualRecord = academicRecordRepo.findAllByApplicant(applicant).stream()
                .filter(r -> requiredQual.getName() != null
                        && r.getQualificationLevel() != null
                        && requiredQual.getName().equalsIgnoreCase(r.getQualificationLevel()))
                .findFirst()
                .orElse(null);

        if (qualRecord == null) {
            saveResult(application, programme, false, "Missing record for: " + requiredQual.getName());
            return;
        }

        List<SubjectMark> studentMarks = qualRecord.getSubjectMarks() != null
                ? qualRecord.getSubjectMarks()
                : List.of();

        // Min overall percentage (optional)
        if (criteria.getMinOverallPercentage() != null) {
            double globalPct = calculatePercentage(studentMarks);
            double required = criteria.getMinOverallPercentage() - relaxation;

            if (globalPct < required) {
                saveResult(application, programme, false,
                        String.format("Overall %.2f%% < Required %.2f%% (incl. %.1f%% relaxation)",
                                globalPct, required, relaxation));
                return;
            }
        }

        // If CUET required, ensure CUET data exists
        if (criteria.isCuetRequired() && applicant.getCuetScore() == null) {
            saveResult(application, programme, false, "CUET is required but CUET data is missing.");
            return;
        }

        List<EligibilityRuleSet> ruleSets = criteria.getRuleSets() != null ? criteria.getRuleSets() : List.of();
        if (ruleSets.isEmpty()) {
            saveResult(application, programme, true, "Eligible (Base qualification criteria satisfied).");
            return;
        }

        // OR across rulesets
        boolean eligible = false;
        StringBuilder finalReason = new StringBuilder();

        for (EligibilityRuleSet rs : ruleSets) {
            if (rs == null) continue;

            StringBuilder pathReason = new StringBuilder();
            boolean pass = checkRuleSet(rs, applicant, studentMarks, relaxation, pathReason);

            if (pass) {
                eligible = true;
                finalReason = new StringBuilder("Eligible (Satisfied rule: ")
                        .append(rs.getDescription() != null ? rs.getDescription() : "RuleSet")
                        .append(").");
                break;
            } else {
                if (pathReason.length() > 0) {
                    finalReason.append("Rule failed (")
                            .append(rs.getDescription() != null ? rs.getDescription() : "RuleSet")
                            .append("): ")
                            .append(pathReason)
                            .append(" | ");
                }
            }
        }

        saveResult(application, programme, eligible,
                eligible ? finalReason.toString() : finalReason.toString().trim());
    }

    private boolean checkRuleSet(EligibilityRuleSet ruleSet,
                                 Applicant applicant,
                                 List<SubjectMark> qualificationMarks,
                                 double relaxation,
                                 StringBuilder reason) {

        List<SubjectRequirement> requirements = ruleSet.getSubjectRequirements() != null
                ? ruleSet.getSubjectRequirements()
                : List.of();

        if (requirements.isEmpty()) {
            reason.append("No requirements configured.");
            return false;
        }

        // AND across requirements
        for (SubjectRequirement req : requirements) {
            if (req == null) continue;

            List<String> requiredNames = normalizeSubjects(req.getSubjectNames());
            if (requiredNames.isEmpty()) {
                reason.append("No subjects configured in requirement. ");
                return false;
            }

            boolean ok;
            if (req.getScoreSource() == ScoreSource.NON_CUET) {
                ok = evalNonCuet(req, requiredNames, qualificationMarks, relaxation, reason);
            } else if (req.getScoreSource() == ScoreSource.CUET) {
                ok = evalCuet(req, requiredNames, applicant, relaxation, reason);
            } else {
                reason.append("Invalid score source: ").append(req.getScoreSource()).append(". ");
                return false;
            }

            if (!ok) return false;
        }

        return true;
    }

    private boolean evalNonCuet(SubjectRequirement req,
                                List<String> requiredNames,
                                List<SubjectMark> qualificationMarks,
                                double relaxation,
                                StringBuilder reason) {

        Map<String, BigDecimal> byName = new HashMap<>();
        for (SubjectMark sm : qualificationMarks) {
            if (sm == null || sm.getSubject() == null || sm.getSubject().getSubjectName() == null) continue;
            byName.putIfAbsent(sm.getSubject().getSubjectName().trim().toLowerCase(Locale.ROOT),
                    BigDecimal.valueOf(sm.getPercentage()));
        }

        // pick only configured subjects
        List<String> missing = new ArrayList<>();
        List<BigDecimal> values = new ArrayList<>();
        for (String s : requiredNames) {
            BigDecimal v = byName.get(s.trim().toLowerCase(Locale.ROOT));
            if (v == null) missing.add(s);
            else values.add(v);
        }

        if (!missing.isEmpty()) {
            reason.append("Missing subject(s): ").append(String.join(", ", missing)).append(". ");
            return false;
        }

        BigDecimal min = BigDecimal.valueOf(Optional.ofNullable(req.getMinScore()).orElse(0.0) - relaxation);

        if (req.getCalculationType() == CalculationType.AGGREGATE_AVERAGE) {
            BigDecimal sum = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal avg = sum.divide(BigDecimal.valueOf(values.size()), 6, RoundingMode.HALF_UP);
            if (avg.compareTo(min) < 0) {
                reason.append("NON-CUET average ").append(avg).append(" < required ").append(min).append(". ");
                return false;
            }
            return true;
        }

        if (req.getCalculationType() == CalculationType.INDIVIDUAL_SUBJECT) {
            for (int i = 0; i < requiredNames.size(); i++) {
                String subj = requiredNames.get(i);
                BigDecimal v = values.get(i);
                if (v.compareTo(min) < 0) {
                    reason.append("NON-CUET ").append(subj).append(" ").append(v)
                            .append(" < required ").append(min).append(". ");
                    return false;
                }
            }
            return true;
        }

        reason.append("Invalid calculationType: ").append(req.getCalculationType()).append(". ");
        return false;
    }

    private boolean evalCuet(SubjectRequirement req,
                             List<String> requiredNames,
                             Applicant applicant,
                             double relaxation,
                             StringBuilder reason) {

        CuetScore cuet = applicant.getCuetScore();
        if (cuet == null) {
            reason.append("CUET data missing. ");
            return false;
        }

        List<CuetSubjectScore> subjectScores = cuet.getSubjectScores() != null ? cuet.getSubjectScores() : List.of();

        Map<String, BigDecimal> byCode = new HashMap<>();
        for (CuetSubjectScore css : subjectScores) {
            if (css == null) continue;

            String code = css.getPaperCode();
            if (code == null || code.isBlank()) continue;

            BigDecimal v = css.getScore() ;
            if (v == null) continue;

            byCode.putIfAbsent(code.trim().toUpperCase(Locale.ROOT), v);
        }

        List<String> missing = new ArrayList<>();
        List<BigDecimal> values = new ArrayList<>();
        for (String code : requiredNames) {
            BigDecimal v = byCode.get(code.trim().toUpperCase(Locale.ROOT));
            if (v == null) missing.add(code);
            else values.add(v);
        }

        if (!missing.isEmpty()) {
            reason.append("Missing CUET subject(s): ").append(String.join(", ", missing)).append(". ");
            return false;
        }

        BigDecimal min = BigDecimal.valueOf(Optional.ofNullable(req.getMinScore()).orElse(0.0) - relaxation);

        if (req.getCalculationType() == CalculationType.AGGREGATE_AVERAGE) {
            BigDecimal sum = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal avg = sum.divide(BigDecimal.valueOf(values.size()), 6, RoundingMode.HALF_UP);
            if (avg.compareTo(min) < 0) {
                reason.append("CUET average ").append(avg).append(" < required ").append(min).append(". ");
                return false;
            }
            return true;
        }

        if (req.getCalculationType() == CalculationType.INDIVIDUAL_SUBJECT) {
            for (int i = 0; i < requiredNames.size(); i++) {
                String subj = requiredNames.get(i);
                BigDecimal v = values.get(i);
                if (v.compareTo(min) < 0) {
                    reason.append("CUET ").append(subj).append(" ").append(v)
                            .append(" < required ").append(min).append(". ");
                    return false;
                }
            }
            return true;
        }

        reason.append("Invalid calculationType: ").append(req.getCalculationType()).append(". ");
        return false;
    }

    private List<String> normalizeSubjects(String[] arr) {
        if (arr == null || arr.length == 0) return List.of();
        return Arrays.stream(arr)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }

    private double resolveRelaxation(EligibilityCriteria criteria, Applicant applicant) {
        if (criteria.getCategoryRelaxations() == null) return 0.0;
        if (applicant.getCommunityCategory() == null) return 0.0;

        String applicantCatCode = applicant.getCommunityCategory().getCategoryCode();
        if (applicantCatCode == null || applicantCatCode.isBlank()) return 0.0;

        return criteria.getCategoryRelaxations().stream()
                .filter(r -> r.getCategoryCode() != null
                        && r.getCategoryCode().trim().equalsIgnoreCase(applicantCatCode.trim()))
                .mapToDouble(EligibilityCategoryRelaxation::getRelaxationValue)
                .findFirst()
                .orElse(0.0);
    }

    private double calculatePercentage(List<SubjectMark> marks) {
        if (marks == null || marks.isEmpty()) return 0.0;

        double obtained = marks.stream().mapToDouble(SubjectMark::getMarksObtained).sum();
        double total = marks.stream().mapToDouble(SubjectMark::getTotalMarks).sum();

        return total > 0 ? (obtained / total) * 100.0 : 0.0;
    }

    // CHANGED: short windowId to String admissionCode
    @Override
    public List<EligibilityResultResponseDTO> getEligibilityForProgramme(String admissionCode, int progId, ApplicantType type) {
        // Look up by code
        AdmissionWindow window = admissionWindowRepository.findByAdmissionCode(admissionCode)
                .orElseThrow(() -> new RuntimeException("Admission window not found for code: " + admissionCode));
        Short windowId = window.getAdmissionId();

        return resultRepo
                .findByProgramme_ProgrammeIdAndApplication_AdmissionWindow_AdmissionIdAndApplication_ApplicantType(
                        progId, windowId, type
                )
                .stream()
                .map(result -> {
                    EligibilityResultResponseDTO dto = new EligibilityResultResponseDTO();
                    dto.setProgrammeName(result.getProgramme().getProgrammeName());
                    dto.setStatus(Boolean.TRUE.equals(result.getIsEligible()) ? "Eligible" : "Not Eligible");
                    dto.setReason(result.getRejectionReason());
                    return dto;
                })
                .toList();
    }

    // CHANGED: short windowId to String admissionCode
    @Override
    public List<EligibilityListRowDTO> getEligibilityListRowsForProgramme(String admissionCode, int progId, ApplicantType type) {
        // Look up by code
        AdmissionWindow window = admissionWindowRepository.findByAdmissionCode(admissionCode)
                .orElseThrow(() -> new RuntimeException("Admission window not found for code: " + admissionCode));
        Short windowId = window.getAdmissionId();

        return resultRepo.findEligibilityListRows(windowId, progId, type);
    }

    private void saveResult(Application app, Programme programme, boolean eligible, String reason) {
        EligibilityResult res = new EligibilityResult();
        res.setApplication(app);
        res.setProgramme(programme);
        res.setIsEligible(eligible);
        res.setRejectionReason(
                reason != null && reason.length() > 255
                        ? reason.substring(0, 252) + "..."
                        : reason
        );
        resultRepo.save(res);
    }
}