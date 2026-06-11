package nic.meg.mcap.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class EligibilityCriteriaRequestDTO {

    private String admissionCode;
    private Short programmeId;

    private Long baseQualificationId;
    private Double minOverallPercentage;

    private List<CategoryRelaxationDTO> categoryRelaxations;

    private Boolean cuetRequired;

    // One place for both CUET and NON-CUET
    private List<EligibilityRuleSetRequestDTO> ruleSets;

    private List<MeritRuleSetRequestDTO> meritRuleSets;

    private String tiebreakerConfig;
}
