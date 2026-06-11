package nic.meg.mcap.dto.request;

import lombok.Data;

@Data
public class SubjectMarkDTO {
    private Integer subjectId;
    private Double marksObtained;
    private Double totalMarks;
    private String subjectName;
}