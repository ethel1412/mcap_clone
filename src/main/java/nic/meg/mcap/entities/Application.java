package nic.meg.mcap.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nic.meg.mcap.enums.ApplicantType;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long applicationId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "applicant_id", nullable = false)
    @JsonIgnore
    private Applicant applicant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "admission_id", nullable = false)
    private AdmissionWindow admissionWindow;

    @Column(unique = true, nullable = false, length = 50)
    private String applicationNo;

    @Column(nullable = false)
    private LocalDateTime applicationDate;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean isDocumentsFinalized = false;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean personalDetailsComplete = false;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean academicDetailsComplete = false;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean programmeSelectionComplete = false;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean paymentComplete = false;

    @Column(precision = 10, scale = 2)
    private BigDecimal amountPaid;

    @Column(length = 100)
    private String transactionId;

    private LocalDateTime paymentTimestamp;

    @Column(length = 20)
    private String applicationStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "applicant_type", nullable = false)
    private ApplicantType applicantType;

    @OneToMany(mappedBy = "application", fetch = FetchType.LAZY)
    private List<ApplicantProgrammePreference> applicantProgrammePreferences;
    
    @PrePersist
    protected void onCreate() {
        this.applicationDate = LocalDateTime.now();
        this.applicationStatus = "INCOMPLETE";
    }
}