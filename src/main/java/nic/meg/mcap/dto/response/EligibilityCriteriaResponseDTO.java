package nic.meg.mcap.dto.response;

import lombok.Data;
import nic.meg.mcap.dto.request.CategoryRelaxationDTO;
import java.util.List;

@Data
public class EligibilityCriteriaResponseDTO {

    private Short eligibilityCriteriaId;

    private Long programmeId;
    private String programmeName;

    private Long baseQualificationId;
    private Double minOverallPercentage;

    private List<CategoryRelaxationDTO> categoryRelaxations;

    private boolean cuetRequired;

    // One place for both CUET and NON-CUET
    private List<EligibilityRuleSetResponseDTO> ruleSets;

    private List<MeritRuleSetResponseDTO> meritRuleSets;

    private String tiebreakerConfig;
}
