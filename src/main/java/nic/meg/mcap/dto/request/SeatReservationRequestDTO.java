package nic.meg.mcap.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import nic.meg.mcap.enums.ReservationType;
import nic.meg.mcap.enums.ApplicantType;
import nic.meg.mcap.enums.ScoreSource;

import java.math.BigDecimal;

@Getter
@Setter
public class SeatReservationRequestDTO {

    @NotNull(message = "Programme offered ID is required")
    private Integer programmeOfferedId;

    @NotNull(message = "Reservation type is required")
    private ReservationType reservationType;

    // For community reservations
    private String categoryCode;

    private Integer reservedSeats;

    // private Integer minimumScore;

    @NotNull(message = "Admission window ID is required")
    private Short admissionWindowId;

    @NotNull(message = "Applicant type is required")
    private ApplicantType applicantType;   // WITH_ENTRANCE / WITHOUT_ENTRANCE

    private ScoreSource examSource;
    // Percentage of total seats to reserve for this band
    @NotNull(message = "Reserved percentage is required")
    private BigDecimal reservedPercentage; // e.g. 60.00


}
