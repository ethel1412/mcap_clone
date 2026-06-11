package nic.meg.mcap.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "department_requests")
@Getter
@Setter
@NoArgsConstructor
public class DepartmentRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Long requestId;

    // Links to your existing Institute entity
    // JPA automatically maps this to the 'institute_id' column (SMALLINT in DB)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institute_id", nullable = false)
    private Institute institute;

    @Column(name = "department_name", nullable = false, length = 100)
    private String departmentName;

    @Column(name = "department_code", length = 20)
    private String departmentCode;

    // Proposed Details
    @Column(name = "hod_name", length = 100)
    private String hodName;

    @Column(length = 100)
    private String email;

    @Column(length = 15)
    private String phone;

    // Status: "PENDING", "APPROVED", "REJECTED"
    // Using String to ensure compatibility with the Service logic
    @Column(nullable = false, length = 50)
    private String status = "PENDING";

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}