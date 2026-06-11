package nic.meg.mcap.dto.response;

import lombok.Getter;
import lombok.Setter;
import nic.meg.mcap.enums.Shift;

@Getter
@Setter
public class ProgrammeOfferedResponseDTO {

    private Integer programmeOfferedId;

    private Integer instituteDepartmentId;

    private Short instituteId;
    private String instituteName;
    private String universityName;

    private Short departmentId;
    private String departmentName;

    private Short programmeId;
    private String programmeName;

    private String programmeLevel;
    private Short streamId;
    private String streamName;

    private Shift shift;
    private String shiftDisplayName;
    
    private String prospectusUrl;
}
