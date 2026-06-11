package nic.meg.mcap.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import nic.meg.mcap.enums.Shift;
import nic.meg.mcap.enums.SubjectType;

@Entity
@Getter
@Setter
public class AvailableSubject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "programme_offered_id", nullable = false)
    private ProgrammeOffered programmeOffered;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Shift shift;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubjectType subjectType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;
}