package nic.meg.mcap.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
public class MeritListRowDTO {

    private Integer rank;

    private Long applicationId;
    private String applicationNo;
    private String applicantName;

    private String category;

    private BigDecimal class12Percentage;
    private BigDecimal ugDegreePercentage;     // NULL for UG

    private BigDecimal entranceScore;
    private String entranceExamType;           // JEE, CUET, GATE, NET

    private BigDecimal normalizedClass12Score; //  for display / debugging
    private BigDecimal normalizedEntranceScore;

    private BigDecimal meritScore;

    // --- NEW FIELD ADDED ---
    private String shift;

    private String selectionCriteria;
    private String ruleDescription;
    private String tieBreakerReason;

    private String applicantType;              // "WITH_ENTRANCE" / "WITHOUT_ENTRANCE"
    private List<String> subjectsUsed;
    private Map<String, BigDecimal> subjectScores;

}