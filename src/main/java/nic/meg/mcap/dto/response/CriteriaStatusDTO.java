package nic.meg.mcap.dto.response;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CriteriaStatusDTO {
    private boolean approved;
    private boolean rejected;
    private String remarks; // optional remarks about the status
}
