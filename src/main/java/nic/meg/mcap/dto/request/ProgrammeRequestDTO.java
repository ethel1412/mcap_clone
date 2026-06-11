package nic.meg.mcap.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import nic.meg.mcap.enums.ProgrammeLevel;

@Getter
@Setter
public class ProgrammeRequestDTO {

    @NotBlank(message = "Programme name cannot be blank.")
    private String programmeName;

    @NotNull(message = "A stream must be selected.")
    private Short streamId;

    @NotNull(message = "Programme level is required.")
    private ProgrammeLevel programmeLevel;

    @NotNull(message = "Department is required.")
    private Integer instituteDepartmentId;

    
}