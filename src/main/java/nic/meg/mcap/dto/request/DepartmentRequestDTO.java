package nic.meg.mcap.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DepartmentRequestDTO {

    @NotBlank(message = "Department Name is required")
    @Size(max = 100, message = "Department Name cannot exceed 100 characters")
    private String departmentName;

    @Size(max = 20, message = "Department Code cannot exceed 20 characters")
    private String departmentCode;

    @Size(max = 100)
    private String hodName;

    @Pattern(
    	    regexp = "^$|^[A-Za-z0-9+_.-]+@(.+)$",
    	    message = "Invalid email format"
    	)
    	private String email;

    	@Pattern(
    	    regexp = "^$|[0-9]{10}",
    	    message = "Phone number must be 10 digits"
    	)
    	private String phone;
}