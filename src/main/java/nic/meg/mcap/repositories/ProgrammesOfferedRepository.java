package nic.meg.mcap.repositories;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.transaction.Transactional;
import nic.meg.mcap.entities.AdmissionWindow;
import nic.meg.mcap.entities.Institute;
import nic.meg.mcap.entities.InstituteDepartment;
import nic.meg.mcap.entities.Programme;
import nic.meg.mcap.entities.ProgrammeOffered;
import nic.meg.mcap.enums.ProgrammeLevel;
import nic.meg.mcap.enums.Shift;
import nic.meg.mcap.enums.InstituteStatus; // <-- Added this import

public interface ProgrammesOfferedRepository extends JpaRepository<ProgrammeOffered, Integer> {

    // --- NEW METHOD FOR SHIFT-BASED DASHBOARD ---
    @EntityGraph(attributePaths = {
            "programme"
    })
    List<ProgrammeOffered> findByInstituteDepartment_Institute_InstituteIdAndShift(
            Short instituteId,
            Shift shift
    );
    // --------------------------------------------


    @EntityGraph(attributePaths = {
            "instituteDepartment",
            "instituteDepartment.institute",
            "programme"
    })
    List<ProgrammeOffered> findByProgramme_ProgrammeIdIn(Set<Short> programmeIds);

    @Query("SELECT co FROM ProgrammeOffered co JOIN FETCH co.programme c WHERE co.instituteDepartment.institute.instituteId = :instituteId")
    List<ProgrammeOffered> findByInstituteIdWithProgramme(@Param("instituteId") Short instituteId);

    List<ProgrammeOffered> findByProgramme_ProgrammeName(String programmeName);

    @Query("SELECT DISTINCT po FROM ProgrammeOffered po " +
            "JOIN FETCH po.instituteDepartment id " +
            "JOIN FETCH id.institute i " +
            "WHERE po.programme.programmeId = :programmeId " +
            "AND i.status = :status " +
            "AND i.active = true")
    List<ProgrammeOffered> findByProgrammeProgrammeId(
            @Param("programmeId") Short programmeId,
            @Param("status") InstituteStatus status
    );

    @Query("SELECT DISTINCT po FROM ProgrammeOffered po " + "JOIN FETCH po.programme p " + "JOIN FETCH p.stream s "
            + "JOIN FETCH po.instituteDepartment id " + "JOIN FETCH id.institute i " + "WHERE s.streamId = :streamId "
            + "AND i.status = nic.meg.mcap.enums.InstituteStatus.ACCEPTED "
            + "AND i.active = true "
            + "ORDER BY p.programmeName ASC")
    List<ProgrammeOffered> findDistinctProgrammesByStreamId(@Param("streamId") Short streamId);

    @Query("SELECT po FROM ProgrammeOffered po WHERE po.instituteDepartment.instituteDepartmentId = :instituteDepartmentId")
    List<ProgrammeOffered> findByInstituteDepartmentId(@Param("instituteDepartmentId") Integer instituteDepartmentId);

    @Query("SELECT DISTINCT po.programme.programmeLevel FROM ProgrammeOffered po WHERE po.instituteDepartment.institute.instituteId = :instituteId")
    List<String> findDistinctProgrammeLevelsByInstitute(@Param("instituteId") Short instituteId);

    @Query("SELECT po FROM ProgrammeOffered po WHERE po.programme.programmeLevel = :level AND po.instituteDepartment.institute.instituteId = :instituteId")
    List<ProgrammeOffered> findByProgrammeLevelAndInstitute(@Param("level") ProgrammeLevel level,
                                                            @Param("instituteId") Short instituteId);

    List<ProgrammeOffered> findByProgramme_Stream_StreamId(Short streamId);

    boolean existsByProgrammeAndInstituteDepartmentAndShift(nic.meg.mcap.entities.Programme programme,
                                                            nic.meg.mcap.entities.InstituteDepartment instituteDepartment, Shift shift);

    Optional<ProgrammeOffered> findByProgrammeAndInstituteDepartment_Institute(Programme programme,
                                                                               Institute institute);

    @Query("SELECT po FROM ProgrammeOffered po " +
            "JOIN FETCH po.programme p " +
            "JOIN FETCH p.stream s " +
            "WHERE s.streamId IN :streamIds " +
            "AND (:programmeLevel IS NULL OR p.programmeLevel = :programmeLevel)")
    List<ProgrammeOffered> findByStreamProgramme(@Param("streamIds") List<Short> streamIds,
                                                 @Param("programmeLevel") ProgrammeLevel programmeLevel);

    Optional<ProgrammeOffered> findByProgrammeProgrammeIdAndInstituteDepartmentInstituteInstituteId(Short programmeId,
                                                                                                    Short instituteId);

    Optional<ProgrammeOffered> findByProgrammeProgrammeIdAndInstituteDepartmentInstituteInstituteIdAndShift(
            Short programmeId, Short instituteId, Shift shift);

    @Query("SELECT po FROM ProgrammeOffered po " +
            "WHERE po.programme = :programme " +
            "AND po.instituteDepartment.institute = :institute")
    List<ProgrammeOffered> findByProgrammeAndInstitute(@Param("programme") Programme programme,
                                                       @Param("institute") Institute institute);

    long countByInstituteDepartment_Institute_InstituteId(Short instituteId);

    List<ProgrammeOffered> findByProgrammeAndInstituteDepartment(Programme programme,
                                                                 InstituteDepartment instituteDepartment);

    @Modifying
    @Transactional
    int deleteByProgramme_ProgrammeIdAndInstituteDepartment_InstituteDepartmentId(Short programmeId,
                                                                                  Integer instituteDepartmentId);

    @Query("""
              SELECT po FROM ProgrammeOffered po
              JOIN FETCH po.programme p
              JOIN FETCH p.stream s
              WHERE s.streamId IN :streamIds
              AND p.programmeLevel = :programmeLevel
          """)
    List<ProgrammeOffered> findByStreamIdsAndLevel(@Param("streamIds") List<Short> streamIds,
                                                   @Param("programmeLevel") ProgrammeLevel programmeLevel);

    Collection<ProgrammeOffered> findByProgramme_ProgrammeLevel(ProgrammeLevel level);

    Collection<ProgrammeOffered> findByProgramme_ProgrammeLevelAndProgramme_Stream(ProgrammeLevel level,
                                                                                   nic.meg.mcap.entities.Stream stream);

    @Query("""
              SELECT po FROM ProgrammeOffered po
              JOIN po.programme p
              WHERE p.programmeLevel = :level
              AND p.stream.streamId = :streamId
          """)
    List<ProgrammeOffered> findByLevelAndStreamId(@Param("level") ProgrammeLevel level,
                                                  @Param("streamId") Short streamId);

    @EntityGraph(attributePaths = {
            "instituteDepartment",
            "instituteDepartment.institute",
            "instituteDepartment.department",
            "programme",
            "programme.stream"
    })
    List<ProgrammeOffered> findWithDetailsByInstituteDepartment_Institute_InstituteId(Short instituteId);

    List<ProgrammeOffered> findByInstituteDepartment_Institute_InstituteId(Short instituteId);


    @EntityGraph(attributePaths = {
            "instituteDepartment",
            "instituteDepartment.institute",
            "instituteDepartment.department",
            "programme",
            "programme.stream"
    })
    List<ProgrammeOffered> findWithAllDetailsByInstituteDepartment_Institute_InstituteId(Short instituteId);

    /**
     * Returns all ProgrammeOffered rows where the institute is both ACCEPTED and active.
     * Used by public-facing pages (home, participating-institutes, applicant programme selection).
     */
    @EntityGraph(attributePaths = {
            "instituteDepartment",
            "instituteDepartment.institute",
            "instituteDepartment.department",
            "programme",
            "programme.stream"
    })
    @Query("""
            SELECT po FROM ProgrammeOffered po
            JOIN po.instituteDepartment id
            JOIN id.institute i
            WHERE i.status = nic.meg.mcap.enums.InstituteStatus.ACCEPTED
              AND i.active = true
            """)
    List<ProgrammeOffered> findAllByActiveAndAcceptedInstitutes();
}