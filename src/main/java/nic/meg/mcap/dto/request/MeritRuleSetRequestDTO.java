package nic.meg.mcap.dto.request;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class MeritRuleSetRequestDTO {

    private String sourceType;

    private Integer optionIndex;

    private String label;

    private List<String> meritSubjects;
}