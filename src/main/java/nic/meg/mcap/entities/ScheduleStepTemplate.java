package nic.meg.mcap.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nic.meg.mcap.audit.AuditingEntityListener;
import nic.meg.mcap.enums.ScheduleActorRole;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class ScheduleStepTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long templateId;

    @Column(nullable = false, unique = true)
    private Integer stepOrder;

    @Column(nullable = false)
    private String stepName;

    @Column(nullable = false)
    private String category; // "PRE_ADMISSION" or "COUNSELLING"

    private String admissionRoute; // e.g., "CUET", "NON_CUET", "GENERAL"

    @Column(length = 500)
    private String description;

    private Integer phaseNumber; // Replaces roundNumber

    @Column(nullable = false)
    private Boolean isActive = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScheduleActorRole defaultActorRole;
}