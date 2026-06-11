package nic.meg.mcap.dto.response;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;


@Getter
@Setter
public class SmsResponse {

    @NotNull(message = "success flag is required")
    private Boolean success;

    @NotBlank(message = "message cannot be empty")
    @Size(max = 200, message = "message cannot exceed 200 characters")
    private String message;

    @Size(max = 50, message = "errorCode cannot exceed 50 characters")
    @Pattern(regexp = "^[A-Z_]+$", message = "errorCode must be uppercase with underscores")
    private String errorCode;

    
    public SmsResponse(Boolean success, String message,String errorCode) {
        this.success = success;
        this.message = message;
        this.errorCode = errorCode;
    }
}
