package nic.meg.mcap.dto.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DistrictDTO {

	@Positive
	private short districtCode;

	@NotBlank
	@Size(max = 50)
	private String districtName;
}
