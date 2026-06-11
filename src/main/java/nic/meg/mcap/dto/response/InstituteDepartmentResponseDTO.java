package nic.meg.mcap.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InstituteDepartmentResponseDTO {

    private Integer instituteDepartmentId;

    private Short instituteId;
    private String instituteName;

    private Short departmentId;
    private String departmentName;
    private String departmentCode;
    private boolean active;

    private String hodName;
    private String email;
    private String phone;
}
