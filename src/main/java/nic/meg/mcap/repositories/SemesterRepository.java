package nic.meg.mcap.repositories;

import nic.meg.mcap.entities.Semester;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SemesterRepository extends JpaRepository<Semester, Long> {

    List<Semester> findByProgrammeOfferedProgrammeOfferedIdAndActiveTrue(Integer programmeOfferedId);

    List<Semester> findByProgrammeOfferedProgrammeOfferedIdOrderBySemesterNumberAsc(Integer programmeOfferedId);

    boolean existsByProgrammeOfferedProgrammeOfferedIdAndSemesterNumber(Integer programmeOfferedId, Integer semesterNumber);

    Optional<Semester> findByProgrammeOfferedProgrammeOfferedIdAndSemesterNumber(Integer programmeOfferedId, Integer semesterNumber);

    @Query("SELECT s FROM Semester s WHERE s.programmeOffered.instituteDepartment.institute.instituteId = :instituteId AND s.active = true")
    List<Semester> findByInstituteIdAndActiveTrue(@Param("instituteId") Short instituteId);

    @Query("SELECT COUNT(sa) FROM SubjectAssignment sa WHERE sa.semester.semesterId = :semesterId AND sa.active = true")
    Long countAssignedSubjects(@Param("semesterId") Long semesterId);
}
