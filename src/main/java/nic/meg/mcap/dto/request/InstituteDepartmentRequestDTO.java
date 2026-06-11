package nic.meg.mcap.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InstituteDepartmentRequestDTO {

    @NotNull(message = "Institute ID is required")
    private Short instituteId;

    @NotNull(message = "Department ID is required")
    private Short departmentId;

    private boolean active = true;

    @Size(max = 120)
    private String hodName;

    @Size(max = 120)
    private String email;

    @Size(max = 20)
    private String phone;
}
