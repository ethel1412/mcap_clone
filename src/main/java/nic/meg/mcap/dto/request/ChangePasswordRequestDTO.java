// src/main/java/nic/meg/mcap/dto/request/UserPasswordChangeRequestDTO.java
package nic.meg.mcap.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nic.meg.mcap.utils.SecurityConstants; // Import

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordRequestDTO {

    @NotBlank(message = "Current password is required")
    private String currentPassword;

    @NotBlank(message = "New password is required")
    private String newPassword;

    @NotBlank(message = "Confirm new password is required")
    private String confirmPassword;

    public boolean isNewPasswordConfirmed() {
        return newPassword != null && newPassword.equals(confirmPassword);
    }
}