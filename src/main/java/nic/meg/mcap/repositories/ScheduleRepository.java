package nic.meg.mcap.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import nic.meg.mcap.entities.Schedule;
import nic.meg.mcap.enums.ScheduleActorRole;
import nic.meg.mcap.entities.AdmissionWindow;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    List<Schedule> findByAdmissionWindowOrderByStepOrderAsc(AdmissionWindow admissionWindow);

    @Query("SELECT s FROM Schedule s WHERE s.admissionWindow.admissionId = :admissionId ORDER BY s.stepOrder ASC")
    List<Schedule> findByAdmissionWindowIdOrderByStepOrder(@Param("admissionId") Short admissionId);

    //  Fetch schedules belonging to a specific phase (PRE_ADMISSION vs COUNSELLING) ---
    @Query("SELECT s FROM Schedule s WHERE s.admissionWindow.admissionId = :admissionId AND s.category = :category ORDER BY s.stepOrder ASC")
    List<Schedule> findByAdmissionWindowIdAndCategoryOrderByStepOrder(
            @Param("admissionId") Short admissionId,
            @Param("category") String category
    );

    Optional<Schedule> findByAdmissionWindowAndStepOrder(AdmissionWindow admissionWindow, Integer stepOrder);

    @Query("SELECT MAX(s.stepOrder) FROM Schedule s WHERE s.admissionWindow.admissionId = :admissionId")
    Optional<Integer> findMaxStepOrderByAdmissionWindow(@Param("admissionId") Short admissionId);

    boolean existsByAdmissionWindowAndStepOrder(AdmissionWindow admissionWindow, Integer stepOrder);

    @Query("SELECT s FROM Schedule s WHERE s.admissionWindow.admissionId = :admissionId AND s.stepName = :stepName")
    Optional<Schedule> findByAdmissionWindowIdAndStepName(
            @Param("admissionId") Short admissionId,
            @Param("stepName") String stepName
    );

    // Existing Allotment Logic - KEPT
    @Query("SELECT s FROM Schedule s " +
            "WHERE s.admissionWindow.admissionId = :admissionId " +
            "AND s.stepName LIKE :stepName " +
            "AND CURRENT_TIMESTAMP BETWEEN s.startDate AND s.endDate " +
            "ORDER BY s.stepOrder DESC")
    Optional<Schedule> findLatestAllotmentScheduleForWindow(
            @Param("admissionId") Short admissionId,
            @Param("stepName") String stepName);

    @Query("SELECT s FROM Schedule s " +
            "WHERE s.admissionWindow.admissionId = :admissionId " +
            "AND s.stepName LIKE :stepName " +
            "AND CURRENT_TIMESTAMP BETWEEN s.startDate AND s.endDate " +
            "ORDER BY s.stepOrder DESC")
    Optional<Schedule> findLatestAllotmentProcessScheduleForWindow(
            @Param("admissionId") Short admissionId,
            @Param("stepName") String stepName);

    @Query("SELECT s FROM Schedule s " +
            "WHERE s.admissionWindow.admissionId = :admissionId " +
            "AND s.stepName LIKE :stepName " +
            "AND s.stepOrder = :nextStepOrder")
    Optional<Schedule> findDecisionScheduleForRound(
            @Param("admissionId") Short admissionId,
            @Param("nextStepOrder") Integer nextStepOrder,
            @Param("stepName") String stepName);
    // Existing Notifications Logic - KEPT
    @Query("SELECT s FROM Schedule s " +
            "JOIN s.admissionWindow aw " +
            "JOIN ScheduleStepTemplate t ON s.stepOrder = t.stepOrder " +
            "WHERE t.defaultActorRole = :role " +
            "AND s.startDate <= :futureDate " +
            "AND s.endDate >= :currentDate " +
            "ORDER BY s.endDate ASC")
    List<Schedule> findInstituteScheduleNotifications(
            @Param("currentDate") LocalDateTime currentDate,
            @Param("futureDate") LocalDateTime futureDate,
            @Param("role") ScheduleActorRole institute
    );

    // Get all unique rounds (e.g., ["CUET", "NON_CUET"]) configured for this window
    @Query("SELECT DISTINCT s.admissionRoute FROM Schedule s WHERE s.admissionWindow.admissionId = :windowId AND s.category = 'COUNSELLING' AND s.admissionRoute IS NOT NULL ORDER BY s.admissionRoute ASC")
    List<String> findDistinctRoundsForWindow(@Param("windowId") Short windowId);

    // Get all unique phases (e.g., [1, 2, 3]) for a specific round in this window
    @Query("SELECT DISTINCT s.phaseNumber FROM Schedule s WHERE s.admissionWindow.admissionId = :windowId AND s.admissionRoute = :roundType AND s.category = 'COUNSELLING' AND s.phaseNumber IS NOT NULL ORDER BY s.phaseNumber ASC")
    List<Integer> findDistinctPhasesForWindowAndRound(@Param("windowId") Short windowId, @Param("roundType") String roundType);

    // Fetch distinct phases for the given routes
    @Query("SELECT DISTINCT s.phaseNumber FROM Schedule s WHERE s.admissionWindow.admissionId = :windowId AND s.category = 'COUNSELLING' AND s.admissionRoute IN (:routes) ORDER BY s.phaseNumber ASC")
    List<Integer> findDistinctPhasesForWindowAndRoutes(@Param("windowId") Short windowId, @Param("routes") List<String> routes);

    // Fetch the start date of the first step for a specific phase
    @Query("SELECT MIN(s.startDate) FROM Schedule s WHERE s.admissionWindow.admissionId = :windowId AND s.category = 'COUNSELLING' AND s.admissionRoute IN (:routes) AND s.phaseNumber = :phaseNo")
    java.time.LocalDateTime findStartDateForPhase(@Param("windowId") Short windowId, @Param("routes") List<String> routes, @Param("phaseNo") Integer phaseNo);

    /**
     * Find the Seat Acceptance schedule step for a specific admission window, round type, and phase.
     * The step name follows the pattern: "{ROUTE} Phase {N}: Seat Acceptance and Admission Fee Payment"
     * Returns the schedule whose endDate is the decision deadline for applicants.
     */
    @Query("""
            SELECT s FROM Schedule s
            WHERE s.admissionWindow.admissionId = :windowId
              AND s.admissionRoute = :roundType
              AND s.phaseNumber = :phaseNo
              AND s.category = 'COUNSELLING'
              AND LOWER(s.stepName) LIKE '%seat acceptance%'
            ORDER BY s.stepOrder ASC
            """)
    Optional<Schedule> findSeatAcceptanceStep(
            @Param("windowId") Short windowId,
            @Param("roundType") String roundType,
            @Param("phaseNo") Integer phaseNo);
}