package nic.meg.mcap.services.impl.merit;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nic.meg.mcap.dto.request.TieBreakerCriterionDTO;
import nic.meg.mcap.dto.response.MeritListMetadataDTO;
import nic.meg.mcap.entities.AdmissionWindow;
import nic.meg.mcap.entities.Applicant;
import nic.meg.mcap.entities.Application;
import nic.meg.mcap.entities.EligibilityCriteria;
import nic.meg.mcap.entities.MeritList;
import nic.meg.mcap.entities.MeritListEntry;
import nic.meg.mcap.entities.MeritRuleSet;
import nic.meg.mcap.entities.Programme;
import nic.meg.mcap.entities.Stream;
import nic.meg.mcap.entities.CuetSubjectScore;
import nic.meg.mcap.enums.ApplicantType;
import nic.meg.mcap.enums.ProgrammeLevel;
import nic.meg.mcap.repositories.EligibilityCriteriaRepository;
import nic.meg.mcap.repositories.CuetScoreRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class MeritListGenerator {

    private static final String STATUS_PUBLISHED = "PUBLISHED";

    private final MeritScoreCalculator meritScoreCalculator;
    private final TieBreakerService tieBreakerService;
    private final MeritListMapper meritListMapper;
    private final MeritListPersistenceService persistenceService;
    private final EligibilityCriteriaRepository eligibilityCriteriaRepository;
    private final CuetScoreRepository cuetScoreRepository;

    private static final Logger logger = LoggerFactory.getLogger(MeritListGenerator.class);

    public MeritListMetadataDTO generateUGListForApplicantType(AdmissionWindow window, Stream stream,
                                                               Programme programme, List<Application> applications, ApplicantType applicantType, String roundType,
                                                               Integer phaseNo) {
        return generateForApplicantType(window, stream, programme, applications, applicantType, ProgrammeLevel.UG,
                roundType, phaseNo);
    }

    public MeritListMetadataDTO generatePGListForApplicantType(AdmissionWindow window, Programme programme,
                                                               List<Application> applications, ApplicantType applicantType, String roundType, Integer phaseNo) {
        return generateForApplicantType(window, null, programme, applications, applicantType, ProgrammeLevel.PG,
                roundType, phaseNo);
    }

    private MeritListMetadataDTO generateForApplicantType(AdmissionWindow window, Stream streamOrNull,
                                                          Programme programme, List<Application> applications, ApplicantType applicantType, ProgrammeLevel level,
                                                          String roundType, Integer phaseNo) {

        EligibilityCriteria eligibilityCriteria = eligibilityCriteriaRepository
                .findByAdmissionWindowAdmissionCodeAndProgrammeProgrammeId(window.getAdmissionCode(),
                        programme.getProgrammeId())
                .orElseThrow(() -> new IllegalStateException("EligibilityCriteria not configured for " + programme.getProgrammeName()));

        String rt = normalizeRoundType(roundType);
        String targetSourceType = "CUET".equals(rt) ? "CUET" : "NON_CUET";

        // 1. Calculate Scores & Identify Winning Rules
        List<MeritEntry> meritEntries = generateBestScoreEntries(eligibilityCriteria, programme, applications,
                applicantType, level, targetSourceType);

        if (meritEntries.isEmpty())
            return null;

        // 2. Sort & Rank using Tie Breakers
        List<TieBreakerCriterionDTO> tbConfig = tieBreakerService.parseTieBreakerConfig(eligibilityCriteria);
        sortByMeritAndTieBreakers(meritEntries, tbConfig, level);
        tieBreakerService.fillTieBreakerReasons(meritEntries, tbConfig, level);
        tieBreakerService.assignRanks(meritEntries);

        // 3. Prepare and Save Merit List Header
        MeritList meritList = new MeritList();
        meritList.setAdmissionWindow(window);
        meritList.setStream(streamOrNull);
        meritList.setProgramme(programme);
        meritList.setRoundType(rt);
        meritList.setPhaseNo(phaseNo);
        meritList.setGeneratedOn(LocalDateTime.now());
        meritList.setStatus(STATUS_PUBLISHED);
        meritList.setTotalApplicants(meritEntries.size());
        meritList.setApplicantType(applicantType != null ? applicantType.name() : null);

        MeritList savedMeritList = persistenceService.saveMeritList(meritList);

        // 4. Map and Save Merit List Entries
        List<MeritListEntry> entries = meritEntries.stream().map(me -> {
            MeritListEntry e = new MeritListEntry();
            e.setMeritList(savedMeritList);
            e.setApplication(me.application);
            e.setRank(me.rank);
            e.setMeritScore(me.meritScore);
            e.setSelectionCriteria(me.ruleLabel);
            e.setTieBreakerReason(me.tieBreakerReason);
            e.setClass12Percentage(me.class12Pct);
            e.setUgDegreePercentage(me.ugDegreePct);
            e.setEntranceScore(me.entranceScore);
            e.setCategory(me.category);
            e.setApplicantType(me.applicantType != null ? me.applicantType.name() : null);
            e.setNormalizedClass12Score(me.normalizedClass12);
            e.setNormalizedUgDegreeScore(me.normalizedUgDegree);
            e.setNormalizedEntranceScore(me.normalizedEntrance);
            e.setSubjectsUsed(me.subjectsUsed);
            e.setSubjectScores(me.subjectScores);
            return e;
        }).collect(Collectors.toList());

        persistenceService.saveEntries(entries);
        return meritListMapper.toMetadataDTO(savedMeritList);
    }

    private List<MeritEntry> generateBestScoreEntries(EligibilityCriteria eligibilityCriteria, Programme programme,
                                                      List<Application> applications, ApplicantType applicantType, ProgrammeLevel level,
                                                      String targetSourceType) {

        List<MeritRuleSet> rules = eligibilityCriteria.getMeritRuleSets().stream()
                .filter(m -> isSourceType(m, targetSourceType)).collect(Collectors.toList());

        List<MeritEntry> finalEntries = new ArrayList<>();

        for (Application app : applications) {
            BigDecimal maxScore = BigDecimal.ZERO;
            MeritRuleSet bestRule = null;
            boolean qualifies = false;

            // Evaluate all possible rules and pick the highest score
            for (MeritRuleSet rule : rules) {
                try {
                    BigDecimal score = meritScoreCalculator.calculateScore(app.getApplicant(), rule);
                    if (score != null && score.compareTo(BigDecimal.ZERO) > 0) {
                        qualifies = true;
                        if (score.compareTo(maxScore) > 0) {
                            maxScore = score;
                            bestRule = rule;
                        }
                    }
                } catch (Exception ex) {
                    logger.info("Calculation error for applicantNo={} ruleId={}: {}",
                            app.getApplicationNo(), rule.getId(), ex.getMessage());
                }
            }

            MeritEntry entry = new MeritEntry();
            entry.application = app;
            entry.applicantType = applicantType;
            entry.class12Pct = safeClass12Pct(app.getApplicant());
            entry.ugDegreePct = (level == ProgrammeLevel.PG) ? safeUgPct(app.getApplicant()) : null;

            // Entrance Score: Uses updated Calculator to handle paper-wise averages
            entry.entranceScore = (applicantType == ApplicantType.WITH_ENTRANCE)
                    ? safeEntranceScore(app.getApplicant(), level)
                    : BigDecimal.ZERO;

            entry.normalizedClass12 = meritScoreCalculator.normalizeScore(entry.class12Pct, BigDecimal.valueOf(100));
            entry.normalizedUgDegree = (entry.ugDegreePct != null)
                    ? meritScoreCalculator.normalizeScore(entry.ugDegreePct, BigDecimal.valueOf(100))
                    : null;
            entry.normalizedEntrance = meritScoreCalculator.normalizeScore(entry.entranceScore, BigDecimal.valueOf(100));

            entry.category = app.getApplicant().getCommunityCategory() != null
                    ? app.getApplicant().getCommunityCategory().getCategoryName()
                    : "GENERAL";

            // FIX: Populate subject-wise breakdown for the UI display
            entry.subjectScores = new HashMap<>();
            if ("CUET".equals(targetSourceType)) {
                cuetScoreRepository.findByApplicant(app.getApplicant()).ifPresent(cuet -> {
                    for (CuetSubjectScore ss : cuet.getSubjectScores()) {
                        String label = ss.getSubjectName() != null ? ss.getSubjectName() : ss.getPaperCode();
                        BigDecimal val = ss.getPercentile() != null ? ss.getPercentile() : BigDecimal.ZERO;
                        entry.subjectScores.put(label, val);
                    }
                });
            }

            if (qualifies && bestRule != null) {
                entry.meritScore = maxScore;
                entry.ruleLabel = (bestRule.getLabel() != null && !bestRule.getLabel().isBlank()) ? bestRule.getLabel()
                        : "Rule #" + bestRule.getId();
                entry.subjectsUsed = bestRule.getMeritSubjects() != null ? Arrays.asList(bestRule.getMeritSubjects())
                        : List.of();
            } else {
                entry.meritScore = BigDecimal.ZERO;
                entry.subjectsUsed = List.of();
                entry.ruleLabel = "No Matching Rule";
                entry.tieBreakerReason = "Ineligible (Score too low or missing subjects)";
            }
            finalEntries.add(entry);
        }
        return finalEntries;
    }

    private void sortByMeritAndTieBreakers(List<MeritEntry> meritEntries, List<TieBreakerCriterionDTO> tbConfig,
                                           ProgrammeLevel level) {
        meritEntries.sort((e1, e2) -> {
            int cmp = e2.meritScore.compareTo(e1.meritScore);
            if (cmp != 0) return cmp;

            var r = tieBreakerService.compareByTieBreakers(e1.application.getApplicant(), e2.application.getApplicant(),
                    tbConfig, level);

            if (r.cmp == 0) {
                // Final fallback: Alphanumeric Application Number
                String an1 = (e1.application.getApplicationNo() == null) ? "" : e1.application.getApplicationNo();
                String an2 = (e2.application.getApplicationNo() == null) ? "" : e2.application.getApplicationNo();
                return an1.compareToIgnoreCase(an2);
            }
            return -r.cmp; // Descending order
        });
    }

    private BigDecimal safeClass12Pct(Applicant applicant) {
        try {
            return meritScoreCalculator.getClass12Percentage(applicant);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal safeUgPct(Applicant applicant) {
        try {
            return meritScoreCalculator.getUGDegreePercentage(applicant);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal safeEntranceScore(Applicant applicant, ProgrammeLevel level) {
        try {
            return meritScoreCalculator.getEntranceScore(applicant, level);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private boolean isSourceType(MeritRuleSet rule, String expected) {
        if (rule == null || rule.getSourceType() == null || expected == null)
            return false;
        return rule.getSourceType().trim().equalsIgnoreCase(expected.trim());
    }

    private String normalizeRoundType(String roundType) {
        String rt = (roundType == null) ? "CUET" : roundType.trim().toUpperCase(Locale.ROOT);
        if ("NONCUET".equals(rt))
            return "NON_CUET";
        return rt;
    }

    private static class MeritEntry implements TieBreakerService.RankableMeritEntry {
        Application application;
        Integer rank;
        BigDecimal meritScore;
        BigDecimal class12Pct;
        BigDecimal ugDegreePct;
        BigDecimal entranceScore;
        String category;
        String tieBreakerReason;
        String ruleLabel;
        ApplicantType applicantType;
        BigDecimal normalizedClass12;
        BigDecimal normalizedUgDegree;
        BigDecimal normalizedEntrance;
        List<String> subjectsUsed;
        Map<String, BigDecimal> subjectScores;

        @Override public Applicant getApplicant() { return application.getApplicant(); }
        @Override public BigDecimal getMeritScore() { return meritScore; }
        @Override public Integer getRank() { return rank; }
        @Override public void setRank(Integer rank) { this.rank = rank; }
        @Override public String getTieBreakerReason() { return tieBreakerReason; }
        @Override public void setTieBreakerReason(String reason) { this.tieBreakerReason = reason; }
    }
}