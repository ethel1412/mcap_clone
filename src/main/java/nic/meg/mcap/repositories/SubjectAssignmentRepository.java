package nic.meg.mcap.repositories;

import nic.meg.mcap.entities.SubjectAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface SubjectAssignmentRepository extends JpaRepository<SubjectAssignment, Long> {

    List<SubjectAssignment> findBySemesterSemesterIdAndActiveTrue(Long semesterId);

    List<SubjectAssignment> findBySemesterProgrammeOfferedProgrammeOfferedIdAndActiveTrue(Integer programmeOfferedId);

    boolean existsBySemesterSemesterIdAndSubjectSubjectId(Long semesterId, Integer subjectId);

    @Query("""
        SELECT sa.subject.subjectId 
        FROM SubjectAssignment sa 
        WHERE sa.semester.semesterId = :semesterId 
        AND sa.active = true
        """)
    Set<Integer> findAssignedSubjectIdsBySemesterId(@Param("semesterId") Long semesterId);

    @Query("""
        SELECT sa.subject.subjectId 
        FROM SubjectAssignment sa 
        WHERE sa.semester.programmeOffered.programmeOfferedId = :programmeOfferedId 
        AND sa.semester.semesterNumber = :semesterNumber 
        AND sa.active = true
        """)
    Set<Integer> findAssignedSubjectIdsByProgrammeAndSemester(
            @Param("programmeOfferedId") Integer programmeOfferedId,
            @Param("semesterNumber") Integer semesterNumber);

    void deleteBySemesterSemesterId(Long semesterId);
}
