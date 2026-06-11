package nic.meg.mcap.dto.response;

import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import nic.meg.mcap.entities.Stream;
import nic.meg.mcap.enums.ProgrammeLevel;


@Getter
@Setter
@EqualsAndHashCode(of = "programmeId")
public class AllProgrammeResponseDTO {

    private Short programmeId;
    private String programmeName;
    private ProgrammeLevel programmeLevel;

    private Stream stream;  // ✅ instead of streamId + streamName
    public AllProgrammeResponseDTO(
            Short programmeId,
            String programmeName,
            ProgrammeLevel programmeLevel,
            Stream stream) {

        this.programmeId = programmeId;
        this.programmeName = programmeName;
        this.programmeLevel = programmeLevel;
        this.stream = stream;
    }
}