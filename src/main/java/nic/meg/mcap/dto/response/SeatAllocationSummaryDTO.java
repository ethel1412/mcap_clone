package nic.meg.mcap.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SeatAllocationSummaryDTO {

    private String admissionCode;

    // Overall stats for the window
    private Integer totalProgrammes;
    private Integer totalSeats;
    private Integer totalAllotted;
    private Integer totalUnfilled;

    // decision stats (current round+phase)
    private Integer totalAccepted;
    private Integer totalPending;

    // Per-programme breakdown
    private List<ProgrammeAllocationSummaryDTO> programmeSummaries;

    // Error (if any)
    private String errorMessage;

    private boolean canGenerateNextPhase;
    private Integer nextPhaseNumber;
    private boolean canStartNonCuet;
}
