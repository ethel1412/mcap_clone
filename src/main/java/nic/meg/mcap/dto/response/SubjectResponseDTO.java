package nic.meg.mcap.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nic.meg.mcap.enums.SubjectType;

@Getter
@Setter
@NoArgsConstructor
public class SubjectResponseDTO {

    private Integer subjectId;
    private String subjectName;
    private String subjectCode;
    private String subjectType;
}
