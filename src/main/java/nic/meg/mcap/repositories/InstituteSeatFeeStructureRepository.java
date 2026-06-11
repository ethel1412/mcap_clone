package nic.meg.mcap.repositories;

import nic.meg.mcap.entities.InstituteSeatFeeStructure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InstituteSeatFeeStructureRepository extends JpaRepository<InstituteSeatFeeStructure, Long> {

    // Collections (particulars, scopes) are LAZY + @BatchSize(25) on the entity.
    // No @EntityGraph needed — Hibernate loads each collection in a separate batch query.
    List<InstituteSeatFeeStructure> findByUser_UserIdAndActiveTrue(Integer userId);

    Optional<InstituteSeatFeeStructure> findByFeeStructureIdAndUser_UserId(Long feeStructureId, Integer userId);

    /**
     * Finds the fee structure applicable to a specific programmeOffered.
     * Checks direct programme scope first, then stream-level scope.
     */
    @Query("""
        SELECT DISTINCT fs FROM InstituteSeatFeeStructure fs
        JOIN fs.scopes sc
        JOIN fs.particulars p
        WHERE fs.active = true
          AND (
            sc.programmeOffered.programmeOfferedId = :programmeOfferedId
            OR sc.stream.streamId = (
                SELECT po.programme.stream.streamId
                FROM ProgrammeOffered po WHERE po.programmeOfferedId = :programmeOfferedId
            )
          )
        ORDER BY fs.feeStructureId ASC
    """)
    List<InstituteSeatFeeStructure> findApplicableStructures(@Param("programmeOfferedId") Integer programmeOfferedId);
}
