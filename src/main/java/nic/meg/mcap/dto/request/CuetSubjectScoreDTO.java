package nic.meg.mcap.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class CuetSubjectScoreDTO {
    private String paperCode;
    private String subjectName;
    private BigDecimal score;
    private BigDecimal percentile;

}