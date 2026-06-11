package nic.meg.mcap.repositories;

import nic.meg.mcap.entities.ApplicantProgrammePreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ProgrammePreferenceRepository extends JpaRepository<ApplicantProgrammePreference, Long> {

    List<ApplicantProgrammePreference> findByApplicationApplicationIdOrderByPreferenceOrderAsc(Long applicationId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ApplicantProgrammePreference p WHERE p.application.applicationId = :applicationId")
    void deleteByApplicationApplicationId(@Param("applicationId") Long applicationId);

    // UPDATED: Now checks for programmeOfferedId instead of programmeId + instituteId
    boolean existsByApplicationApplicationIdAndProgrammeOfferedProgrammeOfferedId(
            Long applicationId, Integer programmeOfferedId);

    boolean existsByApplicationApplicationId(Long applicationId);

    // UPDATED: Fetches all preferences for a base programme (regardless of shift)
    // Useful for generating the initial Merit List
    @Query("""
       SELECT pref FROM ApplicantProgrammePreference pref
       JOIN FETCH pref.application a
       JOIN FETCH pref.programmeOffered po
       WHERE po.programme.programmeId = :programmeId
         AND a.admissionWindow.admissionId = :windowId
         AND a.applicationStatus = 'SUBMITTED'
       ORDER BY pref.preferenceOrder ASC
       """)
    List<ApplicantProgrammePreference> findAllPreferencesForBaseProgramme(
            @Param("windowId") Short windowId,
            @Param("programmeId") Short programmeId);

    //Fetches preferences for a SPECIFIC shift
    // You will need this for Seat Allotment!
    @Query("""
       SELECT pref FROM ApplicantProgrammePreference pref
       JOIN FETCH pref.application a
       WHERE pref.programmeOffered.programmeOfferedId = :programmeOfferedId
         AND a.admissionWindow.admissionId = :windowId
         AND a.applicationStatus = 'SUBMITTED'
       ORDER BY pref.preferenceOrder ASC
       """)
    List<ApplicantProgrammePreference> findAllPreferencesForSpecificShift(
            @Param("windowId") Short windowId,
            @Param("programmeOfferedId") Integer programmeOfferedId);

}