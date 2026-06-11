package nic.meg.mcap.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import nic.meg.mcap.entities.Applicant;

@Repository
public interface ApplicantRepository extends JpaRepository<Applicant, UUID> {

	@Query(value = "SELECT applicant_no " + "FROM mcap.applicant "
			+ "WHERE SUBSTRING(applicant_no FROM 11 FOR 4) = :year " + "ORDER BY applicant_no DESC "
			+ "LIMIT 1 FOR UPDATE", nativeQuery = true)
	String findLastApplicantNoForYear(@Param("year") String year);

	Optional<Applicant> findByApplicantNo(String applicantNo);

	Optional<Applicant> findByApplicantNoIgnoreCase(String applicantNo);

	Optional<Applicant> findByPhoneNumber(String phoneNumber);

	Optional<Applicant> findByPhoneNumberAndDateOfBirth(String phoneNumber, LocalDate dateOfBirth);

	Optional<Applicant> findByEmail(String email);

	int countByPhoneNumber(String phoneNumber);

	Optional<Applicant> findByEmailAndDateOfBirth(String email, LocalDate parse);

	boolean existsByFirstNameIgnoreCaseAndDateOfBirthAndEmailIgnoreCase(String firstName, LocalDate dateOfBirth,
			String email);

	boolean existsByFirstNameIgnoreCaseAndDateOfBirthAndPhoneNumber(String firstName, LocalDate dateOfBirth,
			String phoneNumber);

	@Query("""
			    SELECT a FROM Applicant a
			    WHERE LOWER(TRIM(a.firstName)) = LOWER(TRIM(:firstName))
			    AND a.dateOfBirth = :dateOfBirth
			    AND TRIM(a.phoneNumber) = TRIM(:phoneNumber)
			    AND LOWER(TRIM(a.email)) = LOWER(TRIM(:email))
			""")
	List<Applicant> findDuplicateWithEmail(@Param("firstName") String firstName,
			@Param("dateOfBirth") LocalDate dateOfBirth, @Param("phoneNumber") String phoneNumber,
			@Param("email") String email);

	@Query("""
			    SELECT a FROM Applicant a
			    WHERE LOWER(TRIM(a.firstName)) = LOWER(TRIM(:firstName))
			    AND a.dateOfBirth = :dateOfBirth
			    AND TRIM(a.phoneNumber) = TRIM(:phoneNumber)
			""")
	List<Applicant> findDuplicateWithoutEmail(@Param("firstName") String firstName,
			@Param("dateOfBirth") LocalDate dateOfBirth, @Param("phoneNumber") String phoneNumber);

	@Query("""
			    SELECT COUNT(a) > 0 FROM Applicant a
			    WHERE LOWER(TRIM(a.email)) = LOWER(TRIM(:email))
			""")
	boolean existsByEmailIgnoreCaseTrimmed(@Param("email") String email);
}