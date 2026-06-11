package nic.meg.mcap.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class CuetSubjectScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuet_score_id", nullable = false)
    private CuetScore cuetScore;

    @Column(length = 100)
    private String subjectName;

    @Column(length = 20)
    private String paperCode;

    @Column(precision = 5, scale = 2)
    private BigDecimal score;

    @Column(precision = 5, scale = 2)
    private BigDecimal percentile;
}