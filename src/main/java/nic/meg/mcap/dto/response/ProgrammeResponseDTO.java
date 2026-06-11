package nic.meg.mcap.dto.response;

import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;

import nic.meg.mcap.enums.ProgrammeLevel;

@Getter
@Setter
@EqualsAndHashCode(of = "programmeId")
public class ProgrammeResponseDTO {

    private Short programmeId;
    private String programmeName;
    private ProgrammeLevel programmeLevel;
    private Short streamId;
    private String streamName;
}
