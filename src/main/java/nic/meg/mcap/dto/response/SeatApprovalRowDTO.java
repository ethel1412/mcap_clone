package nic.meg.mcap.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SeatApprovalRowDTO {
    private Long seatMatrixId;
    private Integer programmeOfferedId; // Needed to fetch reservations
    private String instituteName;
    private String programmeName;
    private String streamName;
    private int totalSeats;
    private LocalDateTime updatedAt;
    private String status;
}