package nic.meg.mcap.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EnumOptionDTO {
    private String code;
    private String label;
    private Short streamId;
}
