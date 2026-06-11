package nic.meg.mcap.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class MeritList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long meritListId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private AdmissionWindow admissionWindow;

    // For UG: stream is NOT NULL, programme is NULL
    @ManyToOne(fetch = FetchType.LAZY)
    private Stream stream;

    // For PG: programme is NOT NULL, stream is NULL
    @ManyToOne(fetch = FetchType.LAZY)
    private Programme programme;

    // NEW: Round + Phase context
    @Column(length = 20, nullable = false)
    private String roundType; // "CUET" or "NON_CUET"

    @Column(nullable = false)
    private Integer phaseNo; // 1..N

    @Column(nullable = false)
    private LocalDateTime generatedOn;

    @Column(length = 20, nullable = false)
    private String status; // DRAFT, PUBLISHED, ARCHIVED

    private Integer totalApplicants;

    @Column(length = 20, nullable = false)
    private String applicantType = "WITH_ENTRANCE"; // WITH_ENTRANCE or WITHOUT_ENTRANCE

    @OneToMany(mappedBy = "meritList", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MeritListEntry> entries = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (generatedOn == null) {
            generatedOn = LocalDateTime.now();
        }
        if (status == null) {
            status = "DRAFT";
        }

        // Defaults so existing generator code doesn't break immediately.
        // We will start setting these explicitly from the service/controller in the next steps.
        if (roundType == null || roundType.isBlank()) {
            roundType = "CUET";
        }
        if (phaseNo == null) {
            phaseNo = 1;
        }
        if (applicantType == null || applicantType.isBlank()) {
            applicantType = "WITH_ENTRANCE";
        }
    }
}
