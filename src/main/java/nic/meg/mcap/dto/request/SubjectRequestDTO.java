package nic.meg.mcap.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SubjectRequestDTO {

    @NotBlank(message = "Subject name is required")
    @Size(max = 150, message = "Subject name cannot exceed 150 characters")
    private String subjectName;

    @Size(max = 20, message = "Subject code cannot exceed 20 characters")
    private String subjectCode;
}
