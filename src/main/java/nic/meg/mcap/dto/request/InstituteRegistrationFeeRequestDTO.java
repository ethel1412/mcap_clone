package nic.meg.mcap.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nic.meg.mcap.enums.Caste;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InstituteRegistrationFeeRequestDTO {

    @NotNull(message = "Caste category is required")
    private Caste caste;

    @NotNull(message = "Registration fee amount is required")
    @Positive(message = "Registration fee must be greater than zero")
    private Double amount;
}
