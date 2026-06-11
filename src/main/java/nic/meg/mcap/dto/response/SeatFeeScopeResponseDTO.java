package nic.meg.mcap.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SeatFeeScopeResponseDTO {
    private Long scopeId;
    /** null when scope is stream-level */
    private Integer programmeOfferedId;
    private String programmeName;
    /** null when scope is programme-level */
    private Short streamId;
    private String streamName;
    /** "PROGRAMME" or "STREAM" */
    private String scopeType;
}
