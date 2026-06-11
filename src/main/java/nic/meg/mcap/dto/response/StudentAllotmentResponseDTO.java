package nic.meg.mcap.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nic.meg.mcap.enums.AllotmentStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentAllotmentResponseDTO {
    private Long allotmentId;
    private String studentName;
    private String studentEmail;
    private String studentPhone;
    private String applicationNumber;
    private String programmeName;
    private String departmentName;
    private AllotmentStatus allotmentStatus;
    private String admissionWindowName;
    private String shiftName;
}