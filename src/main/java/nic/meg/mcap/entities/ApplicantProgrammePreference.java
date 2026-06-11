package nic.meg.mcap.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "applicant_programme_preference")
public class ApplicantProgrammePreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    // THE GOLDEN CHANGE: This single column captures the Institute, Programme, AND Shift!
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "programme_offered_id", nullable = false)
    private ProgrammeOffered programmeOffered;

    @Column(nullable = false)
    private Integer preferenceOrder;

    @Column(nullable = false)
    private Boolean isActive = true;
}