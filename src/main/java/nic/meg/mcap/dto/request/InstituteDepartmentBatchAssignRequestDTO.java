package nic.meg.mcap.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class InstituteDepartmentBatchAssignRequestDTO {

    @NotNull(message = "Institute ID is required")
    private Short instituteId;

    @NotNull(message = "At least one department ID is required")
    private List<Short> departmentIds;

    private boolean active = true;

    @Size(max = 120)
    private String hodName;

    @Size(max = 120)
    private String email;

    @Size(max = 20)
    private String phone;
}
