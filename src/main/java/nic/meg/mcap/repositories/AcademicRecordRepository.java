package nic.meg.mcap.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import nic.meg.mcap.entities.AcademicRecord;
import nic.meg.mcap.entities.Applicant;

@Repository
public interface AcademicRecordRepository extends JpaRepository<AcademicRecord, Long> {
	List<AcademicRecord> findByApplicant(Applicant applicant);

	void deleteByApplicant(Applicant applicant);

	@Query("SELECT ar FROM AcademicRecord ar LEFT JOIN FETCH ar.subjectMarks sm LEFT JOIN FETCH sm.subject WHERE ar.applicant = :applicant")
	List<AcademicRecord> findByApplicantWithDetails(@Param("applicant") Applicant applicant);

	// Find by applicant and qualification level
	List<AcademicRecord> findByApplicantAndQualificationLevel(
	        Applicant applicant, String qualificationLevel);

	// this method to fetch all records for a student
	List<AcademicRecord> findAllByApplicant(Applicant applicant);

//    List<AcademicRecord> findByApplicant_ApplicantNoAndIsLatestTrue(String applicantNo);
}
