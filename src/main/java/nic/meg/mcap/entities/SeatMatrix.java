package nic.meg.mcap.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class SeatMatrix {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer totalSeats;

    private String approvalStatus = "DRAFT";

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    private ProgrammeOffered programmeOffered;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "admission_window_id", nullable = false)
    private AdmissionWindow admissionWindow;

}