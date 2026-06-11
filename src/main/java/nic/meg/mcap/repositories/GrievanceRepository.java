package nic.meg.mcap.repositories;

import nic.meg.mcap.entities.Grievance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GrievanceRepository extends JpaRepository<Grievance, Long> {

    // --- NEW: Securely fetches the next sequence directly from PostgreSQL ---
    @Query(value = "SELECT nextval('mcap.grievance_seq')", nativeQuery = true)
    Long getNextTicketSequence();

    /** All grievances for a given role (Controller, Admin) — newest first */
    List<Grievance> findByConcernedRoleRoleIdOrderBySubmittedAtDesc(String roleId);

    /**
     * Institute-specific: only grievances forwarded to THIS institute,
     * filtered by that institute's role '3' AND institute id.
     */
    @Query("""
           SELECT g FROM Grievance g
           WHERE g.concernedRole.roleId = :roleId
             AND g.concernedInstitute.instituteId = :instituteId
           ORDER BY g.submittedAt DESC
           """)
    List<Grievance> findByRoleAndInstitute(
            @Param("roleId") String roleId,
            @Param("instituteId") Short instituteId);

    List<Grievance> findBySubmittedByOrderBySubmittedAtDesc(String submittedBy);
}