package nic.meg.mcap.dto.response;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NextScheduleStepDTO {
    private Integer nextStepOrder;
    private String nextStepName;
    private boolean allStepsCompleted;
    private Integer totalSteps;
    private Integer completedSteps;
}
