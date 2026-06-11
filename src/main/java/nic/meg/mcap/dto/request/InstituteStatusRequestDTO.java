package nic.meg.mcap.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class InstituteStatusRequestDTO {

    @NotBlank(message = "AISHE ID is required")
    @Pattern(
    	    regexp = "^[A-Z]-\\d{4,6}$",
    	    message = "AISHE ID must be in format like U-12345 (uppercase letter followed by 4–6 digits)"
    	)
    private String identifier;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 100)
    private String email;

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Invalid mobile number")
    private String mobile;
}