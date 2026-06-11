package nic.meg.mcap.dto.response;

import java.util.List;

import lombok.Data;
import nic.meg.mcap.enums.CalculationType;
import nic.meg.mcap.enums.ScoreSource;

@Data
public class SubjectRequirementResponseDTO {
    private Short requirementId;
    private List<String> subjectNames;
    private CalculationType calculationType;
    private Double minScore;
    private ScoreSource scoreSource;
}
