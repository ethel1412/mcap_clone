package nic.meg.mcap.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AdmissionWindowProgrammeResponseDTO {
    private Short id;
    private String programmeName;
    private boolean active;
}