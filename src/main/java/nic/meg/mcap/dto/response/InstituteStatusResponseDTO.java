package nic.meg.mcap.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InstituteStatusResponseDTO {
    private Short instituteId;
    private String instituteName;
    private String status;
    private String rejectionReason;
    private String username;
    private String temporaryPassword; // This will now be populated from User.tempPlaintextPassword
    private String message;
    private boolean requiresPasswordReset;
    private boolean isCorrectionPendingReview;
}