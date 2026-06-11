package nic.meg.mcap.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import nic.meg.mcap.enums.AllotmentStatus;

@Data
public class VerificationRequestDTO {

    private Long allotmentId;
    @NotNull(message = "Verification status cannot be null.")
    private AllotmentStatus status;

    @Size(max = 500, message = "Remarks cannot exceed 500 characters.")
    private String remarks;
}