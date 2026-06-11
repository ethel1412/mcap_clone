package nic.meg.mcap.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class AllottedCandidateRowDTO {

    private Long applicationId;
    private String applicantName;
    private String registrationNumber;

    private String communityCategory;   // ST/SC/OBC/EWS/GENERAL (your choice)
    private String reservationUsed;     // OPEN / SC / ST / etc.

    private String programmeName;
    private String instituteName;

    private String shiftName;

    // Use BigDecimal to match MeritListEntry.meritScore (numeric(10,4)) and avoid precision loss.
    private BigDecimal meritScore;

    private Integer rank;

    private String allotmentStatus;     // PENDING / ACCEPTED / REJECTED ...
}