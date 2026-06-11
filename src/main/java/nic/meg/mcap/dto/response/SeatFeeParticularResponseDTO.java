package nic.meg.mcap.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class SeatFeeParticularResponseDTO {
    private Long particularId;
    private String particularName;
    private BigDecimal amount;
    private int displayOrder;
}
