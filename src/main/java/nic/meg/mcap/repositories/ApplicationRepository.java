package nic.meg.mcap.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import nic.meg.mcap.entities.Application;
import nic.meg.mcap.enums.ApplicantType;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    @Query("""
			    SELECT a
			    FROM Application a
			    JOIN FETCH a.applicant
			    JOIN FETCH a.admissionWindow aw
			    LEFT JOIN FETCH aw.stream
			    LEFT JOIN FETCH aw.admissionWindowProgrammes awc
			    LEFT JOIN FETCH awc.programme
			    WHERE a.applicationId = :applicationId
			""")
    Optional<Application> findByIdWithDetails(@Param("applicationId") Long applicationId);

    // Find all applications by admission window and status
    List<Application> findByAdmissionWindow_AdmissionIdAndApplicationStatus(Short windowId, String status);

    // Find applications for specific programme
    List<Application> findByAdmissionWindow_AdmissionIdAndAdmissionWindow_AdmissionWindowProgrammes_Programme_ProgrammeIdAndApplicationStatus(
            Short windowId, Short programmeId, String status);

    // FIX APPLIED HERE:
    // 1. Changed appPref.programme to appPref.programmeOffered.programme
    // 2. Added appPref.isActive = true to ignore removed preferences
    @Query("""
			    SELECT DISTINCT a
			    FROM Application a
			    JOIN a.admissionWindow aw
			    JOIN EligibilityResult er ON er.application = a
			    JOIN ApplicantProgrammePreference appPref ON appPref.application = a
			    WHERE aw.admissionId = :windowId
			      AND er.programme.programmeId = :programmeId
			      AND appPref.programmeOffered.programme.programmeId = :programmeId
			      AND appPref.isActive = true
			      AND a.applicationStatus = 'SUBMITTED'
			      AND er.isEligible = true
			      AND a.applicantType = :applicantType
			""")
    List<Application> findEligibleByWindowProgrammeAndApplicantType(@Param("windowId") Short windowId,
                                                                    @Param("programmeId") Short programmeId, @Param("applicantType") ApplicantType applicantType);

    Optional<Application> findByApplicationNo(String applicationNo);

    Optional<Application> findByTransactionId(String transactionId);
}