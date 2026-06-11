package nic.meg.mcap.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleStepTemplateDTO {

    private Long templateId;
    private Integer stepOrder;
    private String stepName;
    private String category;        // PRE_ADMISSION / COUNSELLING
    private String admissionRoute;  // CUET / NON_CUET / GENERAL
    private String description;
    private Integer phaseNumber;    // Replaces roundNumber
    private Boolean isActive;
    private String defaultActorRole;

}