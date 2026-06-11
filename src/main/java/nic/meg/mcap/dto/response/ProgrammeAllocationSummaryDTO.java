package nic.meg.mcap.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProgrammeAllocationSummaryDTO {
    private Integer programmeOfferedId;
    private String programmeName;
    private String instituteName;
    private String shiftName;
    private Integer totalSeats;
    private Integer reservedSeats;
    private Integer openSeats;

    private Integer allottedCount;
    private Integer unfilledSeats;

    private Integer pendingCount;
}