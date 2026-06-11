package nic.meg.mcap.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import nic.meg.mcap.validation.ValidPhoneNumber;

@Data
@ValidPhoneNumber
public class RegistrationFormDTO {

	// All the same fields as ApplicantDTO
	private String firstName;
	private String middleName;
	private String lastName;
	@NotEmpty(message = "Country code is required")
	private String countryPhoneCode;
	@NotEmpty(message = "Phone number is required")
	private String phoneNumber;
	private String dateOfBirth;
	private String email;
	private String genderCode;
	private String password;
	@NotBlank(message = "Captcha is required")
	private String captcha;
}