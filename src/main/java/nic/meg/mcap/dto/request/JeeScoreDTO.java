package nic.meg.mcap.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class JeeScoreDTO {
    private String applicationNumber;
    private String rollNumber;
    private Integer yearOfExam;
    private String sessionAppeared;
    private BigDecimal bestNtaScore;
    private Integer allIndiaRank;
}