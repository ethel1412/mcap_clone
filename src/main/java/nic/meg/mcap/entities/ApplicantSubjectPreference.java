package nic.meg.mcap.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import nic.meg.mcap.enums.SubjectType;

@Entity
@Getter
@Setter
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"seat_allotment_id", "subject_type", "preference_order"}),
        @UniqueConstraint(columnNames = {"seat_allotment_id", "subject_type", "subject_id"})
})
public class ApplicantSubjectPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seat_allotment_id", nullable = false)
    private SeatAllotment seatAllotment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubjectType subjectType;

    @Column(nullable = false)
    private int preferenceOrder; // e.g., 1, 2, 3
}