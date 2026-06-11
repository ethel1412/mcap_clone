package nic.meg.mcap.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TieBreakerCriterionDTO {
    private String field;
    private int priority;
}
