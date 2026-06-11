package nic.meg.mcap.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nic.meg.mcap.enums.Shift;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "programme_offered")
public class ProgrammeOffered {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer programmeOfferedId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "institute_department_id", nullable = false)
    private InstituteDepartment instituteDepartment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "programme_id", nullable = false)
    private Programme programme;

    @Enumerated(EnumType.STRING)
    @Column(name = "shift", length = 20, nullable = false)
    private Shift shift = Shift.NA;
}
