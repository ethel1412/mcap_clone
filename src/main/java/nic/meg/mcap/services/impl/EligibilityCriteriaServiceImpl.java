package nic.meg.mcap.services.impl;

import lombok.RequiredArgsConstructor;
import nic.meg.mcap.dto.request.*;
import nic.meg.mcap.dto.response.*;
import nic.meg.mcap.entities.*;
import nic.meg.mcap.enums.ScoreSource;
import nic.meg.mcap.repositories.AdmissionWindowRepository;
import nic.meg.mcap.repositories.EligibilityCriteriaRepository;
import nic.meg.mcap.repositories.ProgrammeRepository;
import nic.meg.mcap.repositories.QualificationRepository;
import nic.meg.mcap.services.EligibilityCriteriaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

// --- NEW IMPORTS ---
import nic.meg.mcap.services.ScheduleHelperService;
import nic.meg.mcap.exception.BadRequestException;

@Service
@RequiredArgsConstructor
public class EligibilityCriteriaServiceImpl implements EligibilityCriteriaService {

    private final EligibilityCriteriaRepository criteriaRepo;
    private final AdmissionWindowRepository windowRepo;
    private final ProgrammeRepository programmeRepository;
    private final QualificationRepository qualificationRepo;

    // --- INJECT HELPER ---
    private final ScheduleHelperService scheduleHelperService;

    // --- THE DATABASE STRING ---
    private static final String ELIGIBILITY_STEP_NAME = "Set Eligibility Rules";

    @Override
    @Transactional
    public EligibilityCriteriaResponseDTO saveCriteria(EligibilityCriteriaRequestDTO requestDTO) {

        if (requestDTO.getAdmissionCode() == null) {
            throw new RuntimeException("Admission window code is required");
        }
        if (requestDTO.getProgrammeId() == null) {
            throw new RuntimeException("Programme id is required");
        }

        // --- SECURITY LOCK USING DB STRING ---
        if (!scheduleHelperService.isWindowInScheduleStep(requestDTO.getAdmissionCode(), ELIGIBILITY_STEP_NAME)) {
            throw new BadRequestException("The timeline for setting eligibility rules is currently closed or not active for this window.");
        }

        AdmissionWindow window = windowRepo.findByAdmissionCode(requestDTO.getAdmissionCode())
                .orElseThrow(() -> new RuntimeException("Admission window not found"));

        Programme programme = programmeRepository.findById(requestDTO.getProgrammeId())
                .orElseThrow(() -> new RuntimeException("Programme not found"));

        // FIXED: Removed the trailing semicolon to properly chain .orElseGet()
        EligibilityCriteria criteria = criteriaRepo
                .findByAdmissionWindowAdmissionCodeAndProgrammeProgrammeId(window.getAdmissionCode(), programme.getProgrammeId())
                .orElseGet(EligibilityCriteria::new);

        criteria.setAdmissionWindow(window);
        criteria.setProgramme(programme);

        criteria.setBaseQualification(
                requestDTO.getBaseQualificationId() != null
                        ? qualificationRepo.findById(requestDTO.getBaseQualificationId()).orElse(null)
                        : null
        );

        criteria.setMinOverallPercentage(requestDTO.getMinOverallPercentage());
        criteria.setCuetRequired(Boolean.TRUE.equals(requestDTO.getCuetRequired()));
        criteria.setTiebreakerConfig(requestDTO.getTiebreakerConfig());

        // ---------------- Category relaxations ----------------
        updateCategoryRelaxations(criteria, requestDTO.getCategoryRelaxations());

        // ---------------- Rule sets (Eligibility) ----------------
        updateEligibilityRuleSets(criteria, requestDTO.getRuleSets());

        // ---------------- Merit Rule Sets (The Refactored Logic) ----------------
        updateMeritRuleSets(criteria, requestDTO.getMeritRuleSets());

        // Save everything in one go (Cascaded)
        EligibilityCriteria saved = criteriaRepo.save(criteria);
        return mapToResponseDTO(saved);
    }

    private void updateCategoryRelaxations(EligibilityCriteria criteria, List<CategoryRelaxationDTO> dtos) {
        if (criteria.getCategoryRelaxations() == null) {
            criteria.setCategoryRelaxations(new ArrayList<>());
        } else {
            criteria.getCategoryRelaxations().clear();
        }

        if (dtos != null) {
            for (CategoryRelaxationDTO relaxDTO : dtos) {
                if (relaxDTO == null) continue;
                if (relaxDTO.getCategoryCode() == null || relaxDTO.getCategoryCode().trim().isBlank()) {
                    throw new IllegalArgumentException("Category relaxation categoryCode is required");
                }

                EligibilityCategoryRelaxation relax = new EligibilityCategoryRelaxation();
                relax.setCategoryCode(relaxDTO.getCategoryCode().trim());
                relax.setRelaxationValue(relaxDTO.getRelaxationValue());
                criteria.getCategoryRelaxations().add(relax);
            }
        }
    }

    private void updateEligibilityRuleSets(EligibilityCriteria criteria, List<EligibilityRuleSetRequestDTO> dtos) {
        if (criteria.getRuleSets() == null) {
            criteria.setRuleSets(new ArrayList<>());
        } else {
            criteria.getRuleSets().clear();
        }

        if (dtos != null) {
            for (EligibilityRuleSetRequestDTO rsReq : dtos) {
                if (rsReq == null) continue;

                EligibilityRuleSet ruleSet = new EligibilityRuleSet();
                ruleSet.setDescription(rsReq.getDescription());

                // REMOVED: ruleSet.setEligibilityCriteria(criteria);
                // The relationship is managed by adding it to criteria.getRuleSets()

                List<SubjectRequirement> subjectReqs = new ArrayList<>();
                if (rsReq.getSubjectRequirements() != null) {
                    for (SubjectRequirementRequestDTO subReq : rsReq.getSubjectRequirements()) {
                        SubjectRequirement req = createSubjectRequirement(subReq);
                        subjectReqs.add(req);
                    }
                }

                if (subjectReqs.isEmpty()) {
                    throw new IllegalArgumentException("Each rule set must contain at least one valid subject requirement");
                }

                ruleSet.setSubjectRequirements(subjectReqs);
                criteria.getRuleSets().add(ruleSet);
            }
        }

        if (criteria.getRuleSets().isEmpty()) {
            throw new IllegalArgumentException("At least one eligibility rule set is required");
        }
    }

    private SubjectRequirement createSubjectRequirement(SubjectRequirementRequestDTO subReq) {
        if (subReq == null) return null;
        if (subReq.getScoreSource() == null ||
                !(subReq.getScoreSource() == ScoreSource.CUET || subReq.getScoreSource() == ScoreSource.NON_CUET)) {
            throw new IllegalArgumentException("Eligibility scoreSource must be CUET or NON_CUET");
        }

        List<String> names = subReq.getSubjectNames();
        if (names == null || names.isEmpty()) {
            throw new IllegalArgumentException("Subject names are required");
        }

        String[] subjectArray = names.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toArray(String[]::new);

        Double threshold = subReq.getMinScore();
        if (subReq.getScoreSource() == ScoreSource.NON_CUET) {
            if (threshold == null || threshold <= 0) {
                throw new IllegalArgumentException("NON_CUET minimum threshold must be > 0");
            }
        }

        SubjectRequirement req = new SubjectRequirement();
        req.setSubjectNames(subjectArray);
        req.setMinScore(threshold);
        req.setCalculationType(subReq.getCalculationType());
        req.setScoreSource(subReq.getScoreSource());
        return req;
    }

    private void updateMeritRuleSets(EligibilityCriteria criteria, List<MeritRuleSetRequestDTO> dtos) {
        if (criteria.getMeritRuleSets() == null) {
            criteria.setMeritRuleSets(new ArrayList<>());
        } else {
            criteria.getMeritRuleSets().clear();
        }

        if (dtos != null) {
            for (MeritRuleSetRequestDTO mDto : dtos) {
                if (mDto == null) continue;

                MeritRuleSet meritRule = new MeritRuleSet();

                // Keep this one! You added the field to MeritRuleSet entity in Step 1.
                meritRule.setEligibilityCriteria(criteria);

                meritRule.setSourceType(mDto.getSourceType());
                meritRule.setOptionIndex(mDto.getOptionIndex());
                meritRule.setLabel(mDto.getLabel());

                // The New Explicit Subject List
                if (mDto.getMeritSubjects() != null) {
                    meritRule.setMeritSubjects(mDto.getMeritSubjects().toArray(new String[0]));
                } else {
                    meritRule.setMeritSubjects(new String[0]);
                }

                criteria.getMeritRuleSets().add(meritRule);
            }
        }
    }

    @Override
    public EligibilityCriteriaResponseDTO getCriteriaByWindowAndProgramme(String admissionCode, Short programmeId) {
        if (admissionCode == null || programmeId == null) return null;

        return criteriaRepo
                // FIXED: Updated to call the new method name from the repository
                .findByAdmissionWindowAdmissionCodeAndProgrammeProgrammeId(admissionCode, programmeId)
                .map(this::mapToResponseDTO)
                .orElse(null);
    }

    private EligibilityCriteriaResponseDTO mapToResponseDTO(EligibilityCriteria entity) {
        EligibilityCriteriaResponseDTO dto = new EligibilityCriteriaResponseDTO();
        dto.setEligibilityCriteriaId(entity.getEligibilityCriteriaId());

        if (entity.getProgramme() != null) {
            dto.setProgrammeId(entity.getProgramme().getProgrammeId().longValue());
            dto.setProgrammeName(entity.getProgramme().getProgrammeName());
        }

        if (entity.getBaseQualification() != null) {
            dto.setBaseQualificationId(entity.getBaseQualification().getId());
        }

        dto.setMinOverallPercentage(entity.getMinOverallPercentage());
        dto.setCuetRequired(entity.isCuetRequired());
        dto.setTiebreakerConfig(entity.getTiebreakerConfig());

        if (entity.getCategoryRelaxations() != null) {
            dto.setCategoryRelaxations(
                    entity.getCategoryRelaxations().stream().map(r -> {
                        CategoryRelaxationDTO rDto = new CategoryRelaxationDTO();
                        rDto.setCategoryCode(r.getCategoryCode());
                        rDto.setRelaxationValue(r.getRelaxationValue());
                        return rDto;
                    }).collect(Collectors.toList())
            );
        }

        if (entity.getRuleSets() != null) {
            dto.setRuleSets(entity.getRuleSets().stream().map(rs -> {
                EligibilityRuleSetResponseDTO rsDTO = new EligibilityRuleSetResponseDTO();
                rsDTO.setRuleSetId(rs.getRuleSetId());
                rsDTO.setDescription(rs.getDescription());

                if (rs.getSubjectRequirements() != null) {
                    rsDTO.setSubjectRequirements(
                            rs.getSubjectRequirements().stream().map(sub -> {
                                SubjectRequirementResponseDTO subDTO = new SubjectRequirementResponseDTO();
                                subDTO.setRequirementId(sub.getRequirementId() != null ? sub.getRequirementId().shortValue() : null);
                                subDTO.setSubjectNames(sub.getSubjectNames() == null ? List.of() : Arrays.asList(sub.getSubjectNames()));
                                subDTO.setCalculationType(sub.getCalculationType());
                                subDTO.setMinScore(sub.getMinScore());
                                subDTO.setScoreSource(sub.getScoreSource());
                                return subDTO;
                            }).collect(Collectors.toList())
                    );
                }
                return rsDTO;
            }).collect(Collectors.toList()));
        }

        if (entity.getMeritRuleSets() != null) {
            dto.setMeritRuleSets(entity.getMeritRuleSets().stream().map(m -> {
                MeritRuleSetResponseDTO md = new MeritRuleSetResponseDTO();
                md.setId(m.getId());
                md.setSourceType(m.getSourceType());
                md.setOptionIndex(m.getOptionIndex());
                md.setRuleIndex(m.getRuleIndex());
                md.setLabel(m.getLabel());

                if (m.getMeritSubjects() != null) {
                    md.setMeritSubjects(Arrays.asList(m.getMeritSubjects()));
                } else {
                    md.setMeritSubjects(new ArrayList<>());
                }

                return md;
            }).collect(Collectors.toList()));
        }
        return dto;
    }
}