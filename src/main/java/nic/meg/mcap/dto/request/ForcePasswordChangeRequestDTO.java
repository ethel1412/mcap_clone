// src/main/java/nic/meg/mcap/dto/request/ForcePasswordChangeRequest.java
package nic.meg.mcap.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import nic.meg.mcap.utils.SecurityConstants;

@Getter
@Setter
@NoArgsConstructor
public class ForcePasswordChangeRequestDTO {

    @NotBlank(message = "New password is required")
    private String newPassword;

    @NotBlank(message = "Confirm new password is required")
    private String confirmPassword;

    public boolean isNewPasswordConfirmed() {
        return newPassword != null && newPassword.equals(confirmPassword);
    }
}