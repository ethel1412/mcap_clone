package nic.meg.mcap.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "merit_rule_set")
public class MeritRuleSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "eligibility_criteria_id", nullable = false)
    private EligibilityCriteria eligibilityCriteria;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "merit_subjects", columnDefinition = "text[]")
    private String[] meritSubjects;

    @Column(length = 20)
    private String sourceType;

    private Integer optionIndex;

    private Integer ruleIndex;

    private String label;
}