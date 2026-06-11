package nic.meg.mcap.dto.response;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class EligibilityResultResponseDTO {
    private String programmeName;
    private String instituteName;
    private String status; // "Eligible" or "Not Eligible"
    private String reason; // The specific reason if rejected
}