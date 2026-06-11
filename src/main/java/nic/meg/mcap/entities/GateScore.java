package nic.meg.mcap.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class GateScore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id", nullable = false, unique = true)
    @JsonIgnore
    private Applicant applicant;

    @Column(length = 20)
    private String registrationNumber;

    @Column(length = 4)
    private String yearOfExam;

    @Column(length = 100)
    private String subject;

    private Double score; // GATE score is out of 1000
}