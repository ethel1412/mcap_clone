package nic.meg.mcap.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PastAcademicRecordRequestDTO {
    private Long id;
    private String qualificationLevel;
    private String boardOrUniversity;
    private String schoolOrCollege;
    private String streamOrMajor;
    private String dateOfPassing;
    private Double percentage;
}