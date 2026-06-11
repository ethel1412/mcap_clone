package nic.meg.mcap.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import nic.meg.mcap.enums.CriteriaApprovalStatus;

@Getter
@Setter
@Entity
public class CriteriaSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private InstituteDepartment instituteDepartment;

    @Column(nullable = false)
    private Short academicYear;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CriteriaApprovalStatus submissionStatus = CriteriaApprovalStatus.DRAFT;

    private java.time.LocalDateTime submittedAt;
    private java.time.LocalDateTime reviewedAt;
    private String reviewerRemarks;

    @Column(updatable = false)
    private java.time.LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
    }
}
