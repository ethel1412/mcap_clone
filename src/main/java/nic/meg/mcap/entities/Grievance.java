package nic.meg.mcap.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "grievance",
        schema = "mcap",
        indexes = {
                @Index(name = "idx_grievance_role",      columnList = "concerned_role_id"),
                @Index(name = "idx_grievance_institute", columnList = "concerned_institute_id")
        }
)
@Getter
@Setter
public class Grievance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** System-generated human-readable code e.g. GRV-2026-1000 */
    @Column(name = "ticket_code", nullable = false, unique = true, length = 20)
    private String ticketCode;

    /** Relates directly to the new GrievanceCategory table */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private GrievanceCategory category;

    /** The applicant's message */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    /** applicantNo of the submitter */
    @Column(name = "submitted_by", nullable = false, length = 100)
    private String submittedBy;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    /**
     * Derived from category mapping: which role should receive this grievance.
     * Note: Trailing spaces have been removed (e.g. '1', '3', '4', '6')
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "concerned_role_id", nullable = false)
    private Role concernedRole;

    /**
     * Only populated for institute-related categories.
     * Points to the specific institute the applicant chose.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concerned_institute_id")
    private Institute concernedInstitute;

    /** OPEN or RESOLVED */
    @Column(nullable = false, length = 20)
    private String status = "OPEN";

    @PrePersist
    void prePersist() {
        if (submittedAt == null) submittedAt = LocalDateTime.now();
    }
}