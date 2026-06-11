package nic.meg.mcap.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ApplicantAddressResponseDTO {

    private String addressLine1;
    private String addressLine2;
    private String pincode;
    private Short stateCode;
    private Short districtCode;
    private String townVillage;
}