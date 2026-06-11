package nic.meg.mcap.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleNotificationResponseDTO {
    private String stepName;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status; // "UPCOMING", "ACTIVE", "ENDING_SOON"
    private Integer daysRemaining;
}
