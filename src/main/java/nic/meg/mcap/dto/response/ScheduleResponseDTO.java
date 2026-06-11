package nic.meg.mcap.dto.response;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleResponseDTO {
    private Long scheduleId;

    // CHANGED: Short admissionId to String admissionCode
    private String admissionCode;

    private String admissionWindowName;
    private Long templateId;      // Link to the master template
    private String category;      // PRE_ADMISSION / COUNSELLING
    private Integer stepOrder;
    private String stepName;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status;
    private String admissionRoute;
    private Integer phaseNumber;
}