package nic.meg.mcap.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SeatAllotmentDecisionRequestDTO {
    @NotNull(message = "Allotment ID cannot be null.")
    private Long allotmentId;
}