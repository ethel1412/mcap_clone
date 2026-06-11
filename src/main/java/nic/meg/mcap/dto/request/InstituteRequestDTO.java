package nic.meg.mcap.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nic.meg.mcap.enums.InstituteStatus;

@Getter
@Setter
@NoArgsConstructor
public class InstituteRequestDTO {

	@Min(value = 1, message = "Invalid institute ID")
	@Max(value = 500, message = "Invalid institute ID")
	private Short instituteId;

	@NotBlank(message = "Institute name is required")
	@Size(max = 100, message = "Institute name cannot exceed 100 characters")
	private String instituteName;

	@Pattern(regexp = "^[A-Z]-\\d{5}$", message = "AISHE ID must be in the format U-12345")
	@NotBlank(message = "AISHE ID is required")
	private String AISHEId;

	@NotNull(message = "Year established is required")
	@Min(value = 1800, message = "Year established should not be before 1800")
	@Max(value = 2025, message = "Year established cannot be in the future")
	private Integer yearEstablished;

	@Size(max = 100, message = "Border district area cannot exceed 100 characters")
	private String borderDistrictArea;

	@NotBlank(message = "University name is required")
	@Size(max = 100, message = "University name cannot exceed 100 characters")
	private String universityName;

	@NotBlank(message = "Institution head details are required")
	@Size(max = 200, message = "Institution head details cannot exceed 200 characters")
	private String institutionHeadDetails;

	@NotBlank(message = "Official email is required")
	@Email(message = "Please provide a valid email address")
	@Size(max = 50, message = "Email cannot exceed 50 characters")
	private String institutionOfficialEmailId;

	@NotBlank(message = "Contact number is required")
	@Pattern(regexp = "^\\d{10}$", message = "Contact number must be exactly 10 digits")
	private String institutionOfficialContactNumber;

	@Pattern(regexp = "^$|^(https?://)?([\\w-]+\\.)+[\\w-]{2,}(:\\d+)?(/\\S*)?$", message = "Please provide a valid website URL")
	@Size(max = 100, message = "Website URL cannot exceed 100 characters")
	private String institutionWebsite;

	@NotNull(message = "Affiliation type is required")
	private Integer affiliationTypeId;

	@NotNull(message = "Management type is required")
	private Integer managementTypeId;

	private InstituteStatus status = InstituteStatus.PENDING;

	@Size(max = 1000, message = "Rejection reason cannot exceed 1000 characters")
	private String rejectionReason;

	@Valid
	@NotNull(message = "Address information is required")
	private AddressDTO addressDTO;

	private String prospectusUrl;
}
