package nic.meg.mcap.repositories;

import nic.meg.mcap.entities.MeritList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MeritListRepository extends JpaRepository<MeritList, Long> {

    // =====================================================================
    // LEGACY METHODS (kept for backward compatibility)
    // =====================================================================

    @Query("""
            SELECT ml FROM MeritList ml
            WHERE ml.admissionWindow.admissionId = :windowId
              AND ml.stream.streamId = :streamId
              AND ml.status = :status
            """)
    Optional<MeritList> findByAdmissionWindowAndStream(
            @Param("windowId") Short windowId,
            @Param("streamId") Short streamId,
            @Param("status") String status
    );

    @Query("""
            SELECT ml FROM MeritList ml
            WHERE ml.admissionWindow.admissionId = :windowId
              AND ml.programme.programmeId = :programmeId
              AND ml.status = :status
            """)
    Optional<MeritList> findByAdmissionWindowAndProgramme(
            @Param("windowId") Short windowId,
            @Param("programmeId") Short programmeId,
            @Param("status") String status
    );

    @Query("""
            SELECT (COUNT(ml) > 0) FROM MeritList ml
            WHERE ml.admissionWindow.admissionId = :windowId
              AND ml.stream.streamId = :streamId
              AND ml.status = :status
            """)
    boolean existsByAdmissionWindowAndStream(
            @Param("windowId") Short windowId,
            @Param("streamId") Short streamId,
            @Param("status") String status
    );

    @Query("""
            SELECT (COUNT(ml) > 0) FROM MeritList ml
            WHERE ml.admissionWindow.admissionId = :windowId
              AND ml.programme.programmeId = :programmeId
              AND ml.status = :status
            """)
    boolean existsByAdmissionWindowAndProgramme(
            @Param("windowId") Short windowId,
            @Param("programmeId") Short programmeId,
            @Param("status") String status
    );

    List<MeritList> findAllByAdmissionWindow_AdmissionIdAndStream_StreamId(
            Short admissionWindowId,
            Short streamId
    );

    List<MeritList> findAllByAdmissionWindow_AdmissionIdAndProgramme_ProgrammeId(
            Short admissionWindowId,
            Short programmeId
    );

    // =====================================================================
    // NEW METHODS (ROUND + PHASE aware)
    // =====================================================================

    // --- ADDED THIS METHOD TO FIX THE "SAME LIST" ISSUE ---
    /**
     * Finds the specific merit list for a single programme.
     * Used by MeritListServiceImpl.getLatestMeritListByProgramme.
     */
    Optional<MeritList> findByAdmissionWindow_AdmissionIdAndProgramme_ProgrammeIdAndRoundTypeAndPhaseNo(
            Short admissionWindowId,
            Short programmeId,
            String roundType,
            Integer phaseNo
    );

    @Query("""
            SELECT ml FROM MeritList ml
            WHERE ml.admissionWindow.admissionId = :windowId
              AND ml.stream.streamId = :streamId
              AND ml.roundType = :roundType
              AND ml.phaseNo = :phaseNo
              AND ml.status = :status
            """)
    Optional<MeritList> findByAdmissionWindowAndStreamAndRoundAndPhaseAndStatus(
            @Param("windowId") Short windowId,
            @Param("streamId") Short streamId,
            @Param("roundType") String roundType,
            @Param("phaseNo") Integer phaseNo,
            @Param("status") String status
    );

    @Query("""
            SELECT ml FROM MeritList ml
            WHERE ml.admissionWindow.admissionId = :windowId
              AND ml.programme.programmeId = :programmeId
              AND ml.roundType = :roundType
              AND ml.phaseNo = :phaseNo
              AND ml.status = :status
            """)
    Optional<MeritList> findByAdmissionWindowAndProgrammeAndRoundAndPhaseAndStatus(
            @Param("windowId") Short windowId,
            @Param("programmeId") Short programmeId,
            @Param("roundType") String roundType,
            @Param("phaseNo") Integer phaseNo,
            @Param("status") String status
    );

    @Query("""
            SELECT ml FROM MeritList ml
            WHERE ml.admissionWindow.admissionId = :admissionWindowId
              AND ml.stream.streamId = :streamId
              AND ml.applicantType = :applicantType
              AND ml.roundType = :roundType
              AND ml.phaseNo = :phaseNo
            """)
    Optional<MeritList> findByAdmissionWindowAndStreamAndApplicantTypeAndRoundAndPhase(
            @Param("admissionWindowId") Short admissionWindowId,
            @Param("streamId") Short streamId,
            @Param("applicantType") String applicantType,
            @Param("roundType") String roundType,
            @Param("phaseNo") Integer phaseNo
    );

    @Query("""
            SELECT ml FROM MeritList ml
            WHERE ml.admissionWindow.admissionId = :admissionWindowId
              AND ml.programme.programmeId = :programmeId
              AND ml.applicantType = :applicantType
              AND ml.roundType = :roundType
              AND ml.phaseNo = :phaseNo
            """)
    Optional<MeritList> findByAdmissionWindowAndProgrammeAndApplicantTypeAndRoundAndPhase(
            @Param("admissionWindowId") Short admissionWindowId,
            @Param("programmeId") Short programmeId,
            @Param("applicantType") String applicantType,
            @Param("roundType") String roundType,
            @Param("phaseNo") Integer phaseNo
    );

    @Query("""
            SELECT CASE WHEN COUNT(ml) > 0 THEN true ELSE false END
            FROM MeritList ml
            WHERE ml.admissionWindow.admissionId = :admissionWindowId
              AND ml.stream.streamId = :streamId
              AND ml.applicantType = :applicantType
              AND ml.roundType = :roundType
              AND ml.phaseNo = :phaseNo
            """)
    Boolean existsByAdmissionWindowAndStreamAndApplicantTypeAndRoundAndPhase(
            @Param("admissionWindowId") Short admissionWindowId,
            @Param("streamId") Short streamId,
            @Param("applicantType") String applicantType,
            @Param("roundType") String roundType,
            @Param("phaseNo") Integer phaseNo
    );

    @Query("""
            SELECT CASE WHEN COUNT(ml) > 0 THEN true ELSE false END
            FROM MeritList ml
            WHERE ml.admissionWindow.admissionId = :admissionWindowId
              AND ml.programme.programmeId = :programmeId
              AND ml.applicantType = :applicantType
              AND ml.roundType = :roundType
              AND ml.phaseNo = :phaseNo
            """)
    Boolean existsByAdmissionWindowAndProgrammeAndApplicantTypeAndRoundAndPhase(
            @Param("admissionWindowId") Short admissionWindowId,
            @Param("programmeId") Short programmeId,
            @Param("applicantType") String applicantType,
            @Param("roundType") String roundType,
            @Param("phaseNo") Integer phaseNo
    );

    @Query("""
            SELECT ml FROM MeritList ml
            WHERE ml.admissionWindow.admissionId = :admissionWindowId
              AND ml.programme.programmeId = :programmeId
              AND ml.roundType = :roundType
              AND ml.phaseNo = :phaseNo
            ORDER BY ml.applicantType
            """)
    List<MeritList> findAllByAdmissionWindowAndProgrammeOrderedByTypeForRoundAndPhase(
            @Param("admissionWindowId") Short admissionWindowId,
            @Param("programmeId") Short programmeId,
            @Param("roundType") String roundType,
            @Param("phaseNo") Integer phaseNo
    );

    void deleteByAdmissionWindowAdmissionIdAndProgrammeProgrammeIdAndApplicantTypeAndRoundTypeAndPhaseNo(
            Short admissionWindowId,
            Short programmeId,
            String applicantType,
            String roundType,
            Integer phaseNo
    );

    void deleteByAdmissionWindowAdmissionIdAndStreamStreamIdAndApplicantTypeAndRoundTypeAndPhaseNo(
            Short admissionWindowId,
            Short streamId,
            String applicantType,
            String roundType,
            Integer phaseNo
    );

    List<MeritList> findAllByAdmissionWindowAdmissionIdAndRoundTypeAndPhaseNoOrderByProgrammeProgrammeIdAsc(
            Short admissionId,
            String roundType,
            Integer phaseNo
    );

    // =====================================================================
    // Resolve latest (roundType, phaseNo) for manage page defaulting
    // =====================================================================

    interface RoundPhaseView {
        String getRoundType();
        Integer getPhaseNo();
    }

    @Query("""
        select ml.roundType as roundType, ml.phaseNo as phaseNo
        from MeritList ml
        where ml.admissionWindow.admissionId = :windowId
        order by
          case when ml.roundType = 'CUET' then 1 else 2 end,
          ml.phaseNo desc,
          ml.generatedOn desc
    """)
    List<RoundPhaseView> findLatestRoundPhase(@Param("windowId") Short windowId);

    @Query("""
      select coalesce(max(ml.phaseNo), 1)
      from MeritList ml
      where ml.admissionWindow.admissionId = :windowId
        and ml.roundType = :roundType
    """)
    Integer findMaxPhaseNoForWindowAndRound(@Param("windowId") Short windowId,
                                            @Param("roundType") String roundType);

}