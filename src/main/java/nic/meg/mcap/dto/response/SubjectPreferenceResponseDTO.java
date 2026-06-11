package nic.meg.mcap.dto.response;

import lombok.Data;
import nic.meg.mcap.enums.Shift;
import nic.meg.mcap.enums.SubjectType;
import java.util.List;
import java.util.Map;

@Data
public class SubjectPreferenceResponseDTO {
    private Shift chosenShift;
    private Map<SubjectType, List<Integer>> preferences;
}