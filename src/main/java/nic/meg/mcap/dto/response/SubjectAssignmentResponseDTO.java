package nic.meg.mcap.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubjectAssignmentResponseDTO {

    private Long assignmentId;
    private Long semesterId;
    private Integer semesterNumber;
    private String semesterName;
    private Integer subjectId;
    private String subjectName;
    private String subjectCode;
    private boolean active;
}
