package nic.meg.mcap.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import nic.meg.mcap.enums.ProgrammeLevel;

@Entity
@Table(uniqueConstraints = {
	    @UniqueConstraint(columnNames = {"admission_window_admission_id", "stream_stream_id"}),
	    @UniqueConstraint(columnNames = {"admission_window_admission_id", "programme_programme_id"})
	})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdmissionCriteria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long criteriaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admission_window_admission_id", nullable = false)
    private AdmissionWindow admissionWindow;

    // Kept for now (legacy / compatibility)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stream_stream_id")
    private Stream stream;

    // Programme-wise criteria (UG + PG)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "programme_programme_id")
    private Programme programme;

    @Enumerated(EnumType.STRING)
    private ProgrammeLevel programmeLevel;

    /**
     * Selected CUET subjects used for merit (JSON array of strings).
     * If empty/null -> merit calculation will fall back to CuetScore.overallPercentile later.
     */
    @Column(columnDefinition = "text")
    private String cuetMeritSubjectsJson;

    /**
     * Selected non-CUET (qualification) subjects used for merit (JSON array of strings).
     * If empty/null -> merit calculation will fall back to AcademicRecord.percentage later.
     */
    @Column(columnDefinition = "text")
    private String nonCuetMeritSubjectsJson;

    /**
     * Tie-breaker config stored as JSON text (existing behavior).
     */
    @Column(columnDefinition = "text")
    private String tiebreakerConfig;

    private boolean isActive = true;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
