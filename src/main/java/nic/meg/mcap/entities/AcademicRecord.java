package nic.meg.mcap.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class AcademicRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id", referencedColumnName = "applicantId", nullable = false)
    private Applicant applicant;

    @Column(nullable = false, length = 50)
    private String qualificationLevel;
    @Column(length = 150)
    private String schoolOrCollege;
    @Column(nullable = false, length = 150)
    private String boardOrUniversity;

    private LocalDate dateOfPassing;
    @Column(nullable = false)
    private boolean latestQualification = false;

    @Column(length = 100)
    private String streamOrMajor;
    private Double percentage;

    @ManyToOne
    @JoinColumn(name = "stream_id")
    private Stream stream;

    @OneToMany(mappedBy = "academicRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SubjectMark> subjectMarks = new ArrayList<>();
}