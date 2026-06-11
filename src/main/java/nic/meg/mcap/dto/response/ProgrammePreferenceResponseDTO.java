package nic.meg.mcap.dto.response;

import lombok.Data;

@Data
public class ProgrammePreferenceResponseDTO {
    private Long id;
    private Long applicationId;
    private Integer programmeOfferedId;

    // Flattened Programme Data
    private Short programmeId;
    private String programmeName;

    // Flattened Stream Data
    private Short streamId;
    private String streamName;

    // Flattened Institute Data
    private Short instituteId;
    private String instituteName;

    private String shift;
    private String shiftDisplayName;

    private Integer preferenceOrder;
    private Boolean isActive;
}