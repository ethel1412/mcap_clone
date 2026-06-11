package nic.meg.mcap.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class AddressFormDTO {

    @NotEmpty(message = "State is required")
    private String stateCode; // <-- The key change is here: String

    // The other fields are not on the registration form, so they are not needed here.
}