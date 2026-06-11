package nic.meg.mcap.dto.response;

public interface MeritRuleSetView {
    Long getId();
    String getLabel();
    String getSourceType();

    // meritRuleSet.subjectRequirement.requirementId
    Long getSubjectRequirementRequirementId();

    Integer getRuleIndex();
    Integer getOptionIndex();
    Integer getMeritOrderIndex();
}
