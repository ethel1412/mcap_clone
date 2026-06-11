package nic.meg.mcap.dto.request;

import lombok.Data;

@Data
public class RequiredProgrammeRequestDTO {
    private Short programmeId;
    private Double minimumPercentage;
}
