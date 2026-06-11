package nic.meg.mcap.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdmissionWindowProgrammeRowDTO {

    private Integer programmeOfferedId;
    private String programmeName;
    private String streamName;
    private String programmeLevel;
    private Integer totalSeats;
    private Integer criteriaCount;
    private boolean hasValidSeats;
    private boolean canEdit;
    private boolean alreadySent;
    private String approvalStatus;

    // --- NEW SHIFT FIELDS ---
    private String shift;
    private String shiftDisplayName;

    /**
     * Institute's declared CUET participation preference for this programme's seat matrix.
     * true  = CUET allotment, false = Non-CUET allotment.
     */
    private boolean wantsCuet;

    public AdmissionWindowProgrammeRowDTO(Integer programmeOfferedId,
                                          String programmeName,
                                          String streamName,
                                          String programmeLevel,
                                          Integer totalSeats,
                                          Integer criteriaCount,
                                          boolean hasValidSeats,
                                          boolean canEdit,
                                          boolean alreadySent,
                                          String approvalStatus,
                                          String shift,
                                          String shiftDisplayName,
                                          boolean wantsCuet) {
        this.programmeOfferedId = programmeOfferedId;
        this.programmeName = programmeName;
        this.streamName = streamName;
        this.programmeLevel = programmeLevel;
        this.totalSeats = totalSeats;
        this.criteriaCount = criteriaCount;
        this.hasValidSeats = hasValidSeats;
        this.canEdit = canEdit;
        this.alreadySent = alreadySent;
        this.approvalStatus = approvalStatus;
        this.shift = shift;
        this.shiftDisplayName = shiftDisplayName;
        this.wantsCuet = wantsCuet;
    }
}