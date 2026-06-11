package nic.meg.mcap.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nic.meg.mcap.enums.ProgrammeLevel;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgrammeWithCriteriaDTO {

    private Short programmeId;
    private String programmeName;
    private ProgrammeLevel programmeLevel;
    private String streamName;
    private boolean hasCriteria; // true if weightage is set
    private AdmissionCriteriaResponseDTO criteria; // null if not set
}
