package nic.meg.mcap.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AcademicRecordDTO {
    private long id;
    private String qualificationLevel;
    private boolean latestQualification = false;
    private String schoolOrCollege;
    private String boardOrUniversity;
    private LocalDate dateOfPassing;
    private String streamOrMajor;
    private Double percentage;
    private List<SubjectMarkDTO> subjectMarks = new ArrayList<>();
}