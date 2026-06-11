package nic.meg.mcap.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class StepPresetDTO {
    private String code;
    private String label;
    private String defaultRole;
    private String description;
}
