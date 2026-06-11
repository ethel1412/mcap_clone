package nic.meg.mcap.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Stores an institute's CUET participation preference for a specific admission window.
 *
 * This is a per-institute, per-window decision — NOT per-programme.
 * All programmes offered by the institute in this window follow the same preference.
 *
 * wantsCuet = true  → institute opts into the CUET allotment route for this window.
 * wantsCuet = false → institute opts into the Non-CUET allotment route (default).
 *
 * preferenceSubmitted is set to true when the institute does Final Submit,
 * after which the preference is locked and cannot be changed.
 */
@Entity
@Table(
        name = "institute_admission_preference",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_institute_window",
                columnNames = {"institute_id", "admission_window_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class InstituteAdmissionPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "institute_id", nullable = false)
    private Institute institute;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "admission_window_id", nullable = false)
    private AdmissionWindow admissionWindow;

    @Column(nullable = false)
    private boolean wantsCuet = false;

    /**
     * Locked to true after Final Submit. Once true, wantsCuet may not be changed.
     */
    @Column(nullable = false)
    private boolean preferenceSubmitted = false;

    public InstituteAdmissionPreference(Institute institute, AdmissionWindow admissionWindow, boolean wantsCuet) {
        this.institute = institute;
        this.admissionWindow = admissionWindow;
        this.wantsCuet = wantsCuet;
    }
}