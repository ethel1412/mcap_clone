package nic.meg.mcap.repositories;

import nic.meg.mcap.dto.response.EligibilityListRowDTO;
import nic.meg.mcap.entities.Application;
import nic.meg.mcap.entities.EligibilityResult;
import nic.meg.mcap.enums.ApplicantType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EligibilityResultRepository extends JpaRepository<EligibilityResult, Short> {

    void deleteByApplication_ApplicationId(Long applicationId);

    List<EligibilityResult> findByApplication_ApplicationId(Long applicationId);

    Optional<EligibilityResult> findByApplication(Application application);

    List<EligibilityResult> findByProgramme_ProgrammeIdAndApplication_AdmissionWindow_AdmissionIdAndApplication_ApplicantType(
            Integer programmeId,
            Short admissionWindowId,
            ApplicantType applicantType
    );

    // FIX: Changed pref.programme to pref.programmeOffered.programme
    @Query("""
        select er
        from EligibilityResult er
        where er.application.admissionWindow.admissionId = :admissionWindowId
          and er.programme.programmeId = :programmeId
          and er.application.applicantType = :applicantType
          and exists (
              select 1
              from ApplicantProgrammePreference pref
              where pref.application = er.application
                and pref.programmeOffered.programme = er.programme
          )
    """)
    List<EligibilityResult> findPreferredByProgrammeAndWindowAndApplicantType(
            @Param("admissionWindowId") short admissionWindowId,
            @Param("programmeId") int programmeId,
            @Param("applicantType") ApplicantType applicantType
    );

    // FIX: Changed pref.programme to pref.programmeOffered.programme
    @Query("""
        select new nic.meg.mcap.dto.response.EligibilityListRowDTO(
            er.application.applicationId,
            er.application.applicationNo,
            concat(
                coalesce(er.application.applicant.firstName, ''), ' ',
                coalesce(er.application.applicant.middleName, ''), ' ',
                coalesce(er.application.applicant.lastName, '')
            ),
            pref.preferenceOrder,
            er.programme.programmeName,
            case when er.isEligible = true then 'Eligible' else 'Not eligible' end
        )
        from EligibilityResult er
        join ApplicantProgrammePreference pref
            on pref.application = er.application
           and pref.programmeOffered.programme = er.programme
        where er.application.admissionWindow.admissionId = :admissionWindowId
          and er.programme.programmeId = :programmeId
          and er.application.applicantType = :applicantType
    """)
    List<EligibilityListRowDTO> findEligibilityListRows(
            @Param("admissionWindowId") short admissionWindowId,
            @Param("programmeId") int programmeId,
            @Param("applicantType") ApplicantType applicantType
    );
}