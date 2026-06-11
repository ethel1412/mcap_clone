package nic.meg.mcap.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class InstituteSeatFeeStructureResponseDTO {
    private Long feeStructureId;
    private String feeName;
    private BigDecimal totalAmount;
    private List<SeatFeeParticularResponseDTO> particulars;
    private List<SeatFeeScopeResponseDTO> scopes;
    /** Human-readable summary of affected programmes/streams for the table column */
    private String scopeSummary;
}
