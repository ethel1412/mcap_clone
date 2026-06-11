package nic.meg.mcap.services.impl.merit;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nic.meg.mcap.dto.request.TieBreakerCriterionDTO;
import nic.meg.mcap.entities.Applicant;
import nic.meg.mcap.entities.EligibilityCriteria;
import nic.meg.mcap.enums.Caste;
import nic.meg.mcap.enums.ProgrammeLevel;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TieBreakerService {

    private final ObjectMapper objectMapper;
    private final MeritScoreCalculator meritScoreCalculator;

    public List<TieBreakerCriterionDTO> parseTieBreakerConfig(EligibilityCriteria criteria) {
        if (criteria == null) return List.of();

        String json = criteria.getTiebreakerConfig();
        if (json == null || json.isBlank()) return List.of();

        try {
            return objectMapper.readValue(json, new TypeReference<List<TieBreakerCriterionDTO>>() {});
        } catch (Exception e) {
            log.error("Failed to parse tie-breaker config JSON", e);
            return List.of();
        }
    }

    public TieBreakResult compareByTieBreakers(
            Applicant a1,
            Applicant a2,
            List<TieBreakerCriterionDTO> config,
            ProgrammeLevel level
    ) {
        if (config == null || config.isEmpty()) return new TieBreakResult(0, null);

        for (TieBreakerCriterionDTO crit : config) {
            String field = crit.getField();
            if (field == null) continue;

            switch (field) {
                case "class12Percentage" -> {
                    var c1 = meritScoreCalculator.getClass12Percentage(a1);
                    var c2 = meritScoreCalculator.getClass12Percentage(a2);
                    int cmp = c2.compareTo(c1);
                    if (cmp != 0) return new TieBreakResult(cmp, "Class XII");
                }
                case "entranceExamPercentile" -> {
                    try {
                        var s1 = meritScoreCalculator.getEntranceScore(a1, level);
                        var s2 = meritScoreCalculator.getEntranceScore(a2, level);
                        int cmp = s2.compareTo(s1);
                        if (cmp != 0) return new TieBreakResult(cmp, "Entrance Exam");
                    } catch (Exception ignored) {
                        // skip if entrance score missing
                    }
                }
                case "ugDegreePercentage" -> {
                    try {
                        var p1 = meritScoreCalculator.getUGDegreePercentage(a1);
                        var p2 = meritScoreCalculator.getUGDegreePercentage(a2);
                        int cmp = p2.compareTo(p1);
                        if (cmp != 0) return new TieBreakResult(cmp, "UG Degree");
                    } catch (Exception ignored) {
                        // skip if UG degree missing
                    }
                }
                case "communityCategory" -> {
                    String code1 = a1.getCommunityCategory() != null ? a1.getCommunityCategory().getCategoryCode() : null;
                    String code2 = a2.getCommunityCategory() != null ? a2.getCommunityCategory().getCategoryCode() : null;

                    Caste caste1 = Caste.fromCategoryCode(code1);
                    Caste caste2 = Caste.fromCategoryCode(code2);

                    int p1 = caste1 != null ? caste1.getPriority() : Integer.MAX_VALUE;
                    int p2 = caste2 != null ? caste2.getPriority() : Integer.MAX_VALUE;

                    int cmp = Integer.compare(p1, p2);
                    if (cmp != 0) return new TieBreakResult(cmp, "Community Category");
                }
                case "dateOfBirth" -> {
                    int cmp = a1.getDateOfBirth().compareTo(a2.getDateOfBirth());
                    if (cmp != 0) return new TieBreakResult(cmp, "Date of Birth");
                }
                case "applicationDateTime" -> {
                    // ignored (Applicant has no createdOn in your current model)
                }
                default -> {
                    // ignore unknown fields
                }
            }
        }

        return new TieBreakResult(0, null);
    }

    public void fillTieBreakerReasons(
            List<? extends RankableMeritEntry> entries,
            List<TieBreakerCriterionDTO> config,
            ProgrammeLevel level
    ) {
        if (config == null || config.isEmpty()) return;

        for (int i = 0; i < entries.size() - 1; i++) {
            RankableMeritEntry current = entries.get(i);
            RankableMeritEntry next = entries.get(i + 1);

            if (current.getMeritScore().compareTo(next.getMeritScore()) == 0) {
                TieBreakResult r = compareByTieBreakers(current.getApplicant(), next.getApplicant(), config, level);
                if (r.reason != null) {
                    if (current.getTieBreakerReason() == null) current.setTieBreakerReason(r.reason);
                    if (next.getTieBreakerReason() == null) next.setTieBreakerReason(r.reason);
                }
            }
        }
    }

    public void assignRanks(List<? extends RankableMeritEntry> entries) {
        for (int i = 0; i < entries.size(); i++) {
            entries.get(i).setRank(i + 1);
        }
    }

    public static class TieBreakResult {
        public final int cmp;
        public final String reason;

        public TieBreakResult(int cmp, String reason) {
            this.cmp = cmp;
            this.reason = reason;
        }
    }

    public interface RankableMeritEntry {
        Applicant getApplicant();
        java.math.BigDecimal getMeritScore();
        Integer getRank();
        void setRank(Integer rank);
        String getTieBreakerReason();
        void setTieBreakerReason(String reason);
    }
}
