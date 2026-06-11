package nic.meg.mcap.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DepartmentRequestResponseDTO {

    private Long requestId;
    private String departmentName;
    private String departmentCode;
    
    // Contact info
    private String hodName;
    private String email;
    private String phone;

    // Status tracking
    private String status; // PENDING, APPROVED, REJECTED
    private String rejectionReason;
    
    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Institute Details (Crucial for the Controller to know who asked)
    private Short instituteId;
    private String instituteName; 
}