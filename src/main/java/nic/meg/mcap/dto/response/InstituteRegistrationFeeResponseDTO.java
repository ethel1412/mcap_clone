package nic.meg.mcap.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nic.meg.mcap.enums.Caste;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InstituteRegistrationFeeResponseDTO {

    private Integer feeId;

    private Caste caste;

    private Double amount;

    private Boolean isActive;

    private String createdAt;

    private String updatedAt;
}