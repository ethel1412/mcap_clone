package nic.meg.mcap.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class EligibilityCategoryRelaxation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category_code", nullable = false)
    private String categoryCode; // e.g., "SC", "ST", "OBC"

    @Column(name = "relaxation_value")
    private Double relaxationValue; // e.g., 5.0
}