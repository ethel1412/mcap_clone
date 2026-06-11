package nic.meg.mcap.entities;

import jakarta.persistence.*;
import lombok.Data;
import nic.meg.mcap.enums.ProgrammeLevel; // Import your Enum
import java.time.LocalDateTime;

@Entity
@Table(name = "programme_requests")
@Data
public class ProgrammeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long requestId;

    @Column(nullable = false)
    private String programmeName;

    // CHANGED: Uses Enum now, stores as String in DB
    @Enumerated(EnumType.STRING) 
    @Column(nullable = false)
    private ProgrammeLevel programmeLevel; 

    @ManyToOne
    @JoinColumn(name = "stream_id", nullable = false)
    private Stream stream;

    @ManyToOne
    @JoinColumn(name = "institute_department_id")
    private InstituteDepartment instituteDepartment;

    @ManyToOne
    @JoinColumn(name = "institute_id", nullable = false)
    private Institute institute;

    private String status; // 'PENDING', 'APPROVED', 'REJECTED'
    
    private String rejectionReason;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}