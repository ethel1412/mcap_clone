package nic.meg.mcap.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nic.meg.mcap.enums.CuetPaperSpec;
import nic.meg.mcap.enums.ProgrammeLevel;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(
        uniqueConstraints = @UniqueConstraint(columnNames = {"programmeLevel", "paperCode"})
)
public class CuetPaper {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ProgrammeLevel programmeLevel; // UG / PG

    // Use String so it supports both UG numeric and PG alphanumeric paper codes
    private String paperCode;

    private String paperName;

    @Enumerated(EnumType.STRING)
    private CuetPaperSpec spec = CuetPaperSpec.OTHER;

    private String domainName; // e.g. SCIENCE, M_TECH, HUMANITIES...

    private boolean isActive = true;

    private Integer sortOrder;
}
