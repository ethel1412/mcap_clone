package nic.meg.mcap.dto.request;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;

@Getter
@Setter
public class ScheduleRequestDTO {

    // CHANGED: Short admissionId to String admissionCode
    @NotNull(message = "Admission window code is required")
    private String admissionCode;

    @NotNull(message = "Step order is required")
    private Integer stepOrder;

    private String stepName;

    private String description;

    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    private LocalDateTime endDate;

    private String admissionRoute;
    private Integer phaseNumber;
}