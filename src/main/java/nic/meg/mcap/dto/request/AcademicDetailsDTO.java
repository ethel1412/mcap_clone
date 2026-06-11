package nic.meg.mcap.dto.request;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AcademicDetailsDTO {
    private Long applicationId;

    private List<LatestAcademicRecordRequestDTO> latestRecords = new ArrayList<>();
    private List<PastAcademicRecordRequestDTO> pastRecords = new ArrayList<>();

    private boolean provideJeeScores = false;
    private boolean provideCuetScores = false;

    private JeeScoreDTO jeeScore = new JeeScoreDTO();
    private CuetScoreDTO cuetScore = new CuetScoreDTO();

    private boolean provideNetScores = false;
    private boolean provideGateScores = false;

    private NetScoreRequestDTO netScore = new NetScoreRequestDTO();
    private GateScoreRequestDTO gateScore = new GateScoreRequestDTO();
}