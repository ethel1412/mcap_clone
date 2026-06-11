package nic.meg.mcap.entities;

import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nic.meg.mcap.audit.AuditingEntityListener;
import nic.meg.mcap.enums.InstituteStatus;

@Entity
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor

public class Institute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Short instituteId;

    @Column(nullable = false)
    private UUID instituteCode;

    @Column(nullable = false, length = 100)
    private String instituteName;

    @Column(name = "aishe_id", nullable = false, unique = true, length = 20)
    private String AISHEId;

    @Column(nullable = false, length = 4)
    private Integer yearEstablished;

    @Column(length = 100)
    private String borderDistrictArea;

    @Column(nullable = false, length = 100)
    private String universityName;

    @Column(nullable = false, length = 200)
    private String institutionHeadDetails;

    @Column(nullable = false, unique = true, length = 50)
    private String institutionOfficialEmailId;

    @Column(nullable = false, unique = true, length = 10)
    private String institutionOfficialContactNumber;

    @Column(nullable = true, length = 100)
    private String institutionWebsite;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private InstituteStatus status = InstituteStatus.PENDING;

    @Column(length = 1000)
    private String rejectionReason;

    @Column(nullable = false)
    private boolean isCorrectionPendingReview = false;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @ManyToOne
    @JoinColumn(name = "affiliation_type_id", nullable = false)
    private AffiliationType affiliationType;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "address_code", nullable = false)
    private Address address;

    @ManyToOne
    @JoinColumn(name = "management_type_id", nullable = false)
    private ManagementType managementType;

    @Column(length = 500, nullable = true)
    private String prospectusUrl;
}