package nic.meg.mcap.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class EligibilityRuleSetRequestDTO {
    private String description; // e.g. "Science Stream"
    private List<SubjectRequirementRequestDTO> subjectRequirements;
}