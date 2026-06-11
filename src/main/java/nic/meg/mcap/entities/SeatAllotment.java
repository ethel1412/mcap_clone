package nic.meg.mcap.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import nic.meg.mcap.enums.AllotmentStatus;
import nic.meg.mcap.enums.Shift;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(
        name = "seat_allotment",
        indexes = {
                @Index(name = "idx_seat_allot_window_round_phase", columnList = "admission_window_id, round_type, phase_no"),
                @Index(name = "idx_seat_allot_po_window_round_phase", columnList = "programme_offered_id, admission_window_id, round_type, phase_no")
        }
)
public class SeatAllotment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id", nullable = false)
    private Applicant applicant;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "admission_window_id", nullable = false)
    private AdmissionWindow admissionWindow;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "programme_offered_id", nullable = false)
    private ProgrammeOffered programmeOffered;

    // NEW: round/phase support
    @Column(name = "round_type", length = 20, nullable = false)
    private String roundType; // "CUET" / "NON_CUET"

    @Column(name = "phase_no", nullable = false)
    private Integer phaseNo;  // 1..N

    @Column(name = "preference_no")
    private Integer preferenceNo;

    // NEW: store which quota bucket was actually used ("SC","ST","OPEN","PWD", etc.)
    @Column(name = "reservation_used", length = 50)
    private String reservationUsed;

    @Enumerated(EnumType.STRING)
    @Column(name = "chosen_shift", length = 20)
    private Shift chosenShift;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private AllotmentStatus status;

    @Column(name = "verification_remarks", length = 500)
    private String verificationRemarks;

    /**
     * Deadline by which the applicant must take a decision (Accept / Slide Up).
     * Set when the allotment moves to PENDING status after institute verification.
     * A scheduled job auto-rejects any PENDING or SLIDE_UP allotment past this timestamp.
     */
    @Column(name = "decision_deadline")
    private LocalDateTime decisionDeadline;

    @OneToMany(mappedBy = "seatAllotment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ApplicantSubjectPreference> subjectPreferences = new ArrayList<>();
}