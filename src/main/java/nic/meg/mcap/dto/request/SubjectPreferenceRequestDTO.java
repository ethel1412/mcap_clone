package nic.meg.mcap.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import nic.meg.mcap.enums.Shift;

import java.util.List;
import java.util.Map;

@Data
public class SubjectPreferenceRequestDTO {

    @NotNull
    private Long seatAllotmentId;

    @NotNull
    private Shift chosenShift;

    private Map<String, List<Integer>> preferences;
}