package nic.meg.mcap.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApplicantCountDTO {

    private int totalComplete;
    private int eligible;

    // Convenience method for eligibility rate (%)
    public double getEligibilityRate() {
        return totalComplete > 0 ? (double) eligible / totalComplete * 100 : 0;
    }

    // Convenience method for eligibility status message
    public String getStatusMessage() {
        if (totalComplete == 0) {
            return "No applications received";
        }
        if (eligible == 0) {
            return "No eligible applicants";
        }
        return String.format("Eligible: %d of %d (%.1f%%)",
                eligible, totalComplete, getEligibilityRate());
    }
}
