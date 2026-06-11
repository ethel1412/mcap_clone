package nic.meg.mcap.repositories;

import nic.meg.mcap.entities.MeritList;
import nic.meg.mcap.entities.MeritListEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

// --- NEW IMPORTS ADDED FOR PAGINATION ---
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Repository
public interface MeritListEntryRepository extends JpaRepository<MeritListEntry, Long> {

    // --- NEW PAGINATED METHOD ADDED ---
    @Query("""
            SELECT e
            FROM MeritListEntry e
            WHERE e.meritList.meritListId = :listId
            ORDER BY e.rank ASC
            """)
    Page<MeritListEntry> findByMeritListIdPaged(@Param("listId") Long listId, Pageable pageable);
    // ----------------------------------

    // Fetch all entries for a merit list ordered by rank (by meritListId)
    @Query("""
            SELECT e
            FROM MeritListEntry e
            WHERE e.meritList.meritListId = :listId
            ORDER BY e.rank ASC
            """)
    List<MeritListEntry> findByMeritListOrderByRank(@Param("listId") Long listId);

    // Fetch entries by category (by meritListId)
    @Query("""
            SELECT e
            FROM MeritListEntry e
            WHERE e.meritList.meritListId = :listId
              AND e.category = :category
            ORDER BY e.rank ASC
            """)
    List<MeritListEntry> findByMeritListAndCategory(
            @Param("listId") Long listId,
            @Param("category") String category
    );

    void deleteByMeritList(MeritList meritList);

    // Fetch all entries for a merit list ordered by rank (by entity)
    List<MeritListEntry> findByMeritListOrderByRank(MeritList meritList);

    /**
     * Fetch the MeritListEntry for a given application within the given admission window + roundType + phaseNo,
     * matching either:
     * - PG: meritList.programme != null and programmeId matches
     * - UG: meritList.stream != null and streamId matches
     *
     * This supports your MeritList design where UG uses stream and PG uses programme.
     */
    @Query("""
            SELECT e
            FROM MeritListEntry e
            JOIN e.meritList ml
            WHERE ml.admissionWindow.admissionId = :admissionId
              AND ml.roundType = :roundType
              AND ml.phaseNo = :phaseNo
              AND e.application.applicationId = :applicationId
              AND (
                   (ml.programme IS NOT NULL AND ml.programme.programmeId = :programmeId)
                OR (ml.stream   IS NOT NULL AND ml.stream.streamId       = :streamId)
              )
            """)
    List<MeritListEntry> findEntryForAllotment( // <-- CHANGED FROM Optional TO List
                                                @Param("admissionId") Short admissionId,
                                                @Param("roundType") String roundType,
                                                @Param("phaseNo") Integer phaseNo,
                                                @Param("applicationId") Long applicationId,
                                                @Param("programmeId") Short programmeId,
                                                @Param("streamId") Short streamId
    );

    /**
     * Existing PG-only helper (kept for backward compatibility).
     * Prefer findEntryForAllotment(...) for UG+PG.
     */
    @Query("""
            SELECT e
            FROM MeritListEntry e
            JOIN e.meritList ml
            WHERE ml.admissionWindow.admissionId = :admissionId
              AND ml.roundType = :roundType
              AND ml.phaseNo = :phaseNo
              AND ml.programme.programmeId = :programmeId
              AND e.application.applicationId = :applicationId
            """)
    Optional<MeritListEntry> findEntryForApplication(
            @Param("admissionId") Short admissionId,
            @Param("roundType") String roundType,
            @Param("phaseNo") Integer phaseNo,
            @Param("programmeId") Short programmeId,
            @Param("applicationId") Long applicationId
    );

    Optional<MeritListEntry> findByApplication_ApplicationIdAndMeritList_Programme_ProgrammeIdAndMeritList_RoundTypeAndMeritList_PhaseNo(
            Long applicationId,
            Long programmeId,
            String roundType,
            Integer phaseNo
    );
}