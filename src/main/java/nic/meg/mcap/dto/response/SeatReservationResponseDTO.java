package nic.meg.mcap.dto.response;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import nic.meg.mcap.enums.ReservationType;
import nic.meg.mcap.enums.ApplicantType;
import nic.meg.mcap.enums.ScoreSource;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SeatReservationResponseDTO {
    private Long id;
    private Integer programmeOfferedId;
    private ReservationType reservationType;
    private String categoryCode;
    private String categoryName;
    private Integer reservedSeats;
    private String displayTitle; // For UI display (e.g., "ST Category", "CUET Qualified")
    // private Integer minimumScore;
    private ApplicantType applicantType;
    private ScoreSource examSource;
    private String examDisplayName;
    private BigDecimal reservedPercentage;

}
