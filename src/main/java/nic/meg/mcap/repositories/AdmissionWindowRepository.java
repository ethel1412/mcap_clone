package nic.meg.mcap.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import nic.meg.mcap.entities.AdmissionWindow;
import nic.meg.mcap.entities.Stream;
import nic.meg.mcap.enums.ProgrammeLevel;

public interface AdmissionWindowRepository extends JpaRepository<AdmissionWindow, Short> {

	List<AdmissionWindow> findByEndDateBeforeAndIsActiveTrue(LocalDateTime dateTime);

	List<AdmissionWindow> findTop5ByOrderByAdmissionIdDesc();

	List<AdmissionWindow> findByIsExtendedTrue();

	@Query("SELECT aw FROM AdmissionWindow aw LEFT JOIN FETCH aw.admissionWindowProgrammes awc LEFT JOIN FETCH awc.programme ORDER BY aw.startDate DESC")
	List<AdmissionWindow> findAllWithProgrammes();

	@Query("SELECT a FROM AdmissionWindow a LEFT JOIN FETCH a.stream WHERE a.isActive = :isActive")
	List<AdmissionWindow> findByIsActiveWithStream(boolean isActive);

	@Query("SELECT aw FROM AdmissionWindow aw JOIN FETCH aw.stream WHERE aw.isActive = :isActive AND aw.programmeLevel = :programmeLevel")
	List<AdmissionWindow> findByIsActiveAndProgrammeLevel(boolean isActive, ProgrammeLevel programmeLevel);

	List<AdmissionWindow> findByIsActive(boolean isActive);

	List<AdmissionWindow> findByStreamAndProgrammeLevel(Stream stream, ProgrammeLevel programmeLevel);

	List<AdmissionWindow> findByIsActiveTrueAndStartDateAfterOrderByStartDateAsc(LocalDateTime now);

	List<AdmissionWindow> findByIsActiveTrueAndProgrammeLevelAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
			ProgrammeLevel programmeLevel, LocalDateTime now1, LocalDateTime now2);

	@Query("""
			SELECT w FROM AdmissionWindow w
			WHERE w.isActive = true
			AND w.startDate <= :now
			AND w.endDate >= :now
			""")
	List<AdmissionWindow> findActiveWindows(@Param("now") LocalDateTime now);

	@Query("""
			SELECT w FROM AdmissionWindow w
			WHERE w.isActive = true
			AND w.endDate < :now
			""")
	List<AdmissionWindow> findClosedWindowsWaitingForCounselling(@Param("now") LocalDateTime now);

	@Query("""
			SELECT w FROM AdmissionWindow w
			WHERE w.isActive = true
			AND w.startDate > :now
			""")
	List<AdmissionWindow> findUpcomingWindows(@Param("now") LocalDateTime now);

	boolean existsByStreamAndProgrammeLevelAndSessionAndStartDateAndEndDate(Stream stream,
			ProgrammeLevel programmeLevel, String session, LocalDateTime startDate, LocalDateTime endDate);

	Optional<AdmissionWindow> findByAdmissionIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(Short admissionId,
			LocalDateTime now, LocalDateTime now2);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
			    SELECT aw FROM AdmissionWindow aw
			    WHERE aw.programmeLevel = :level
			      AND aw.session = :session
			    ORDER BY aw.admissionCode DESC
			""")
	AdmissionWindow findLatestWithLock(ProgrammeLevel level, String session);

	Optional<AdmissionWindow> findByAdmissionCode(String admissionCode);

	boolean existsByStreamIsNullAndProgrammeLevelAndSessionAndStartDateAndEndDate(ProgrammeLevel programmeLevel,
			String session, LocalDateTime startDate, LocalDateTime endDate);

	// ✅ ALL STREAMS (stream = NULL)
	List<AdmissionWindow> findByStreamIsNullAndProgrammeLevelAndSessionAndIsActiveTrueAndAdmissionCodeNot(
			ProgrammeLevel level, String session, String code);

	// ✅ Specific stream
	List<AdmissionWindow> findByStreamAndProgrammeLevelAndSessionAndIsActiveTrueAndAdmissionCodeNot(Stream stream,
			ProgrammeLevel level, String session, String code);

}