package nic.meg.mcap.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "eligibility_rule_set")
public class EligibilityRuleSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Short ruleSetId;

    private String description;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_set_id", nullable = false)
    private List<SubjectRequirement> subjectRequirements = new ArrayList<>();

}
