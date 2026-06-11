package nic.meg.mcap.dto.response;

import lombok.Data;

@Data
public class RequiredProgrammeResponseDTO {
    private Short programmeId;
    private String programmeName;
    private Double minimumPercentage;
}
