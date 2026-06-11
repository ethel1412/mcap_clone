package nic.meg.mcap.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SemesterResponseDTO {

    private Long semesterId;
    private Integer semesterNumber;
    private String semesterName;
    private Integer programmeOfferedId;
    private String programmeName;
    private String departmentName;
    private boolean active;
    private int totalSubjects;
}
