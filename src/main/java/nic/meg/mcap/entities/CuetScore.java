package nic.meg.mcap.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class CuetScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id", nullable = false, unique = true)
    @JsonIgnore
    private Applicant applicant;

    @Column(length = 20)
    private String applicationNumber;

    private Integer yearOfExam;

    @Column(precision = 5, scale = 2)
    private BigDecimal overallPercentile;

    @OneToMany(mappedBy = "cuetScore", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CuetSubjectScore> subjectScores = new ArrayList<>();
}