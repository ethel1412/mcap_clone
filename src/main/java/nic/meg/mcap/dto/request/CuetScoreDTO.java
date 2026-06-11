package nic.meg.mcap.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class CuetScoreDTO {
    private String applicationNumber;
    private Integer yearOfExam;
    private BigDecimal overallPercentile;
    private List<CuetSubjectScoreDTO> subjectScores = new ArrayList<>();

}