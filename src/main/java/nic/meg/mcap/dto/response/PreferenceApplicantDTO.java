package nic.meg.mcap.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PreferenceApplicantDTO {
    private Long applicationId;
    private String applicationNo;
    private String applicantName;
    private Integer preferenceOrder;
    private String programmeName;
    private String instituteName;
    private Boolean isEligible;

}
