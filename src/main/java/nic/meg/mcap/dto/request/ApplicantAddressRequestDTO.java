package nic.meg.mcap.dto.request;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
public class ApplicantAddressRequestDTO {

    @NotEmpty(message = "Address Line 1 is required")
    private String addressLine1;

    private String addressLine2;

    @NotEmpty(message = "Pincode is required")
    @Pattern(regexp = "^[0-9]{6}$", message = "Pincode must be 6 digits")
    private String pincode;

    @NotNull(message = "State is required")
    private Short stateCode;

    @NotNull(message = "District is required")
    private Short districtCode;

    private String townVillage;
}