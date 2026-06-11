package nic.meg.mcap.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class ProgrammeOfferedBatchAssignRequestDTO {

    @NotNull(message = "InstituteDepartment ID is required")
    private Integer instituteDepartmentId;

    @NotNull(message = "At least one Programme ID is required")
    private List<Short> programmeIds;
}
