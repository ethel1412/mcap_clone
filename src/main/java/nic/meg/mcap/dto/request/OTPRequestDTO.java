package nic.meg.mcap.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nic.meg.mcap.enums.OtpPurpose;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OTPRequestDTO {

	@Pattern(regexp = "^\\+\\d{1,4}$", message = "Enter a valid country code (e.g. +91)")
	private String countryCode;

	@Pattern(regexp = "^\\d{6,15}$", message = "Enter a valid phone number")
	private String phoneNumber;

	@Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "Enter a valid email address")
	private String email;

	@NotNull(message = "OTP purpose is required")
	private OtpPurpose purpose;

	@Pattern(regexp = "^\\d{4,6}$", message = "OTP must be 4–6 digits")
	private String otp;

//	@NotBlank(message = "Captcha is required")
	private String captcha;
}