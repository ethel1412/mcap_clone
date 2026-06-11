package nic.meg.mcap.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActiveAdmissionWindowResponseDTO {
    private String admissionCode;
    private String streamName;
    private String programmeLevel;
    private String session;
    private String startDate;
    private String endDate;
    private String status;

    // Extension status
    private boolean extended;
    private String originalEndDate;
}