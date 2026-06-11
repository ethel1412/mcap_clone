package nic.meg.mcap.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import nic.meg.mcap.enums.ProgrammeLevel;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class MeritListMetadataDTO {

    private Long meritListId;

    private Short admissionWindowId;
    private String admissionWindowName;

    private ProgrammeLevel programmeLevel;

    private Short streamId;
    private String streamName;

    private Short programmeId;
    private String programmeName;

    private String applicantType;   // WITH_ENTRANCE / WITHOUT_ENTRANCE

    // --- ADDED FIELDS TO FIX FILTERING ISSUES ---
    private String roundType;       // e.g., CUET, NON-CUET
    private Integer phaseNo;        // e.g., 1, 2, 3

    private Long ruleSetId;         // The specific ID of the rules applied
    private String ruleSetLabel;    // Human-readable label for the rules
    private String sourceType;      // Source of the list (e.g., AUTOMATED)
    // --------------------------------------------

    private LocalDateTime generatedOn;
    private String status;          // DRAFT / PUBLISHED
    private Integer totalApplicants;
}