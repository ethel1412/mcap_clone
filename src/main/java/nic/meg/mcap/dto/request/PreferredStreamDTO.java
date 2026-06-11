package nic.meg.mcap.dto.request;

import lombok.Data;
import nic.meg.mcap.enums.PreferredStreamType;

@Data
public class PreferredStreamDTO {
    private PreferredStreamType streamType;
    private Double minimumPercentage;
}
