package nic.meg.mcap.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class SelectCombinationRequestDTO {

    @NotNull(message = "Seat Allotment ID is required.")
    private Long seatAllotmentId;

    @NotEmpty(message = "At least one combination preference is required.")
    private List<Long> combinationIds;
}