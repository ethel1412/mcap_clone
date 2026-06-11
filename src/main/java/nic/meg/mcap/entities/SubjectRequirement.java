package nic.meg.mcap.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nic.meg.mcap.enums.CalculationType;
import nic.meg.mcap.enums.ScoreSource;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "eligibility_subject_req")
public class SubjectRequirement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long requirementId;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "subject_names", columnDefinition = "text[]", nullable = false)
    private String[] subjectNames;

    @Enumerated(EnumType.STRING)
    @Column(name = "calculation_type", nullable = false)
    private CalculationType calculationType;

    @Column(nullable = true)
    private Double minScore;


    @Enumerated(EnumType.STRING)
    @Column(name = "score_source", nullable = false)
    private ScoreSource scoreSource;
}

