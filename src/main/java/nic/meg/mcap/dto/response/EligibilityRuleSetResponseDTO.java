package nic.meg.mcap.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class EligibilityRuleSetResponseDTO {
    private Short ruleSetId;
    private String description;
    private List<SubjectRequirementResponseDTO> subjectRequirements;
}