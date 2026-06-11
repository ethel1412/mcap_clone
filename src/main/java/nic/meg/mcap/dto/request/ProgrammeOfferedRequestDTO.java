package nic.meg.mcap.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import nic.meg.mcap.enums.Shift;

@Getter
@Setter
public class ProgrammeOfferedRequestDTO {

	@NotNull(message = "InstituteDepartment ID is required")
	private Integer instituteDepartmentId;

	@NotNull(message = "At least one programme must be selected")
    private List<Short> programmeIds; 

	@NotNull(message = "At least one shift is required")
	private List<Shift> shift;
}
