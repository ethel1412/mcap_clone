package nic.meg.mcap.dto.response;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class MeritRuleSetResponseDTO {
    private Long id;
    private String sourceType;
    private Integer optionIndex;
    private Integer ruleIndex;
    private String label;

    // --- NEW FIELD NEEDED ---
    private List<String> meritSubjects;

    // Legacy fields (Keep them if you want to avoid other breakages, or remove if unused)
    private Short subjectRequirementId;
    private Boolean ignoreSpecs;
    private String derivedCodesCsv;
    private String ignoreSubjectsCsv;
    private String derivedSubjectsCsv;
}