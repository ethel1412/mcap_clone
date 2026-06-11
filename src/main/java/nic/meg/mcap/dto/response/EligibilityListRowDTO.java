package nic.meg.mcap.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EligibilityListRowDTO {

    private Long applicationId;
    private String applicationNo;
    private String applicantName;
    private Integer preferenceOrder;
    private String programmeName;
    private String eligibilityStatus;

    public EligibilityListRowDTO(Long applicationId,
                                 String applicationNo,
                                 String applicantName,
                                 Integer preferenceOrder,
                                 String programmeName,
                                 String eligibilityStatus) {
        this.applicationId = applicationId;
        this.applicationNo = applicationNo;
        this.applicantName = applicantName;
        this.preferenceOrder = preferenceOrder;
        this.programmeName = programmeName;
        this.eligibilityStatus = eligibilityStatus;
    }
}

