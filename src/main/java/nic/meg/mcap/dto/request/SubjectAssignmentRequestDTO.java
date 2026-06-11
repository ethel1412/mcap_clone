package nic.meg.mcap.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SubjectAssignmentRequestDTO {

    @NotNull(message = "Semester ID is required")
    private Long semesterId;

    @NotEmpty(message = "At least one subject ID is required")
    private List<Integer> subjectIds;
}
