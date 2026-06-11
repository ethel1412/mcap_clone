package nic.meg.mcap.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScheduleStepTemplateRequestDTO {

    @NotNull(message = "Step order is required")
    private Integer stepOrder;

    @NotBlank(message = "Step name is required")
    private String stepName;

    @NotBlank(message = "Category is required")
    private String category; // PRE_ADMISSION / COUNSELLING

    private String admissionRoute; // CUET / NON_CUET / GENERAL

    private String description;

    private Integer phaseNumber; // Replaces roundNumber

    @NotBlank(message = "Actor role is required")
    private String defaultActorRole;
}