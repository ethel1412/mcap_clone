package nic.meg.mcap.dto.request;

import java.time.LocalDate;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import nic.meg.mcap.validation.ValidPhoneNumber;

@Data
@AllArgsConstructor
@ValidPhoneNumber
public class ApplicantDTO {

	private Short admissionId;
	private String applicantNo;

	@NotEmpty(message = "First name is required")
	@Pattern(regexp = "^[A-Za-z ]{1,50}$", message = "First name must contain only letters and spaces")
	private String firstName;

	@Pattern(regexp = "^[A-Za-z ]{0,50}$", message = "Middle name must contain only letters and spaces")
	private String middleName;

	@NotEmpty(message = "Last name is required")
	@Pattern(regexp = "^[A-Za-z ]{1,50}$", message = "Last name must contain only letters and spaces")
	private String lastName;

	@NotEmpty(message = "Country code is required")
	private String countryPhoneCode;

	@NotEmpty(message = "Phone number is required")
	private String phoneNumber;

	@NotNull(message = "Date of birth is required")
	private LocalDate dateOfBirth;

	@Email(message = "Must be a valid email")
	private String email;

	private String religionCode;
	private String maritalStatusCode;
	private Short countryCode;

	@NotEmpty(message = "Community Category is required")
	private String communityCategoryCode;

	@NotEmpty(message = "Gender is required")
	private String genderCode;

	@Valid
	@NotNull(message = "Permanent address is required")
	private AddressDTO permanentAddress;

	private AddressDTO communicationAddress;

	private boolean isNewUser;

	public ApplicantDTO() {
		this.permanentAddress = new AddressDTO();
		this.communicationAddress = new AddressDTO();
	}
}