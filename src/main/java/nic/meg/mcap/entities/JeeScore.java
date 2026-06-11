package nic.meg.mcap.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class JeeScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id", nullable = false, unique = true)
    @JsonIgnore
    private Applicant applicant;

    @Column(length = 20)
    private String applicationNumber;

    @Column(length = 20)
    private String rollNumber;

    private Integer yearOfExam;

    @Column(length = 20)
    private String sessionAppeared;

    @Column(precision = 5, scale = 2)
    private BigDecimal bestNtaScore;

    private Integer allIndiaRank;
}