package nic.meg.mcap.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
public class SeatAllotmentResponseDTO {

    private Long allotmentId;

    // legacy (keep for now; can be removed later if unused)
    private Long roundScheduleId;

    private String roundName;
    private Short admissionWindowId;

    // rounds + phases
    private String roundType;   // "CUET" / "NON_CUET"
    private Integer phaseNo;    // 1..N

    private String status;

    private String allottedProgramme;
    private String allottedInstitute;

    private String shiftName;

    private int preferenceNumber;

    private LocalDateTime deadline;

    private boolean isFinalRound;

    private String verificationRemarks;

    private LocalDateTime decisionDeadline;
}