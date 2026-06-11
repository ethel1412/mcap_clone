package nic.meg.mcap.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpcomingAdmissionWindowResponseDTO {

    private Short admissionId;
    private String streamName;
    private String programmeLevel;
    private String session;

    private String startDate;
    private String endDate;

    private boolean activeFlag;
    private String status;    // UPCOMING / ACTIVE / EXPIRED / INACTIVE (derived)
}
