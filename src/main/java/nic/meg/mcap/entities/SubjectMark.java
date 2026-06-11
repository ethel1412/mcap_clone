package nic.meg.mcap.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "subject_marks")
@Getter
@Setter
@NoArgsConstructor
public class SubjectMark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_record_id", nullable = false)
    private AcademicRecord academicRecord;

    @ManyToOne
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(nullable = false)
    private Double marksObtained;

    @Column(nullable = false)
    private Double totalMarks;

    @Column(nullable = false)
    private Double percentage;
}