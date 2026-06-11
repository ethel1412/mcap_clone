package nic.meg.mcap.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import nic.meg.mcap.enums.ReservationType;
import nic.meg.mcap.enums.ApplicantType;
import nic.meg.mcap.enums.ScoreSource;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"programme_offered_id", "reservation_type", "category_code"})
})
public class SeatReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "programme_offered_id", nullable = false)
    private ProgrammeOffered programmeOffered;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, name = "reservation_type")
    private ReservationType reservationType; // COMMUNITY or EXAM_QUALIFIED

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "admission_window_id", nullable = false)
    private AdmissionWindow admissionWindow;

    // For community reservations (ST, SC, OBC, GENERAL)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_code")
    private CommunityCategory communityCategory;

    @Column(nullable = false)
    private Integer reservedSeats;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, name = "applicant_type")
    private ApplicantType applicantType; // WITH_ENTRANCE or WITHOUT_ENTRANCE

    // SeatReservation.java
    @Enumerated(EnumType.STRING)
    private ScoreSource examSource; // CUET_UG, CUET_PG, JEE_MAIN, OTHER, etc.

    @Column(name = "reserved_percentage", precision = 5, scale = 2)
    private BigDecimal reservedPercentage;  // e.g., 60.00 means 60%

    // @Column(name = "minimum_score")
    // private Integer minimumScore;
}
