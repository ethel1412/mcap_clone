package nic.meg.mcap.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a named seat-acceptance fee structure defined by an institute.
 * Each structure has multiple line-items (particulars) and can be scoped
 * to specific programmes or entire streams via InstituteSeatFeeScope.
 */
@Entity
@Table(name = "institute_seat_fee_structure")
@Getter
@Setter
@NoArgsConstructor
public class InstituteSeatFeeStructure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long feeStructureId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 150)
    private String feeName;

    @Column(nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Timestamp createdAt;

    @UpdateTimestamp
    private Timestamp updatedAt;

    @OneToMany(mappedBy = "feeStructure", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("displayOrder ASC")
    @BatchSize(size = 25)
    private List<InstituteSeatFeeParticular> particulars = new ArrayList<>();

    @OneToMany(mappedBy = "feeStructure", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @BatchSize(size = 25)
    private List<InstituteSeatFeeScope> scopes = new ArrayList<>();
}
