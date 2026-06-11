package nic.meg.mcap.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CounselingRoundResponseDTO {

    private Long admissionWindowId;
    private String stepName;

    // NEW
    private String roundType;   // "CUET" / "NON_CUET"
    private Integer phaseNo;    // 1..N

    private String status;      // PENDING/ACCEPTED/REJECTED/NOT_ALLOTTED
    private Long allotmentId;
}
