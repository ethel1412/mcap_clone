package nic.meg.mcap.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class LatestAcademicRecordRequestDTO {
    private Long id;
    private String qualificationLevel;
    private String boardOrUniversity;
    private String schoolOrCollege;
    private String streamOrMajor;
    private String dateOfPassing;
    private Double percentage;
    private List<SubjectMarkDTO> subjectMarks = new ArrayList<>();
}