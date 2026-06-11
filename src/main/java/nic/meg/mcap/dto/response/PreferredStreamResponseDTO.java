package nic.meg.mcap.dto.response;

import lombok.Data;
import nic.meg.mcap.enums.PreferredStreamType;

@Data
public class PreferredStreamResponseDTO {
    private Integer id;
    private PreferredStreamType streamType;
    private Double minimumPercentage;
}
