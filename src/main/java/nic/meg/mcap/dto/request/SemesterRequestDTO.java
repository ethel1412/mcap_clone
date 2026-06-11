package nic.meg.mcap.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SemesterRequestDTO {

    @NotNull(message = "Programme offered ID is required")
    private Integer programmeOfferedId;

    @NotNull(message = "Semester number is required")
    @Positive(message = "Semester number must be positive")
    private Integer semesterNumber;

    @Size(max = 100, message = "Semester name cannot exceed 100 characters")
    private String semesterName;

    private boolean active = true;
}
