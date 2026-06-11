package nic.meg.mcap.dto.response;

import lombok.Data;
import nic.meg.mcap.enums.ProgrammeLevel;
import java.time.LocalDateTime;

@Data
public class ProgrammeRequestResponseDTO {
    private Long requestId;
    private String programmeName;
    private ProgrammeLevel programmeLevel;
    private String streamName;
    private String instituteName; // Useful for Admin/Controller view
    private String status;
    private String rejectionReason;
    private LocalDateTime createdAt;
}