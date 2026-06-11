package nic.meg.mcap.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Semester {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long semesterId;

    @NotNull
    @Positive
    @Column(nullable = false)
    private Integer semesterNumber;

    @Column(length = 100)
    private String semesterName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false)
    private ProgrammeOffered programmeOffered;

    @Column(columnDefinition = "boolean default true")
    private boolean active = true;

    @OneToMany(mappedBy = "semester", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SubjectAssignment> subjectAssignments = new ArrayList<>();
}
