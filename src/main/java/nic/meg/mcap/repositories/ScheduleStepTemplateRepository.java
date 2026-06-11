package nic.meg.mcap.repositories;

import nic.meg.mcap.entities.ScheduleStepTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ScheduleStepTemplateRepository extends JpaRepository<ScheduleStepTemplate, Long> {

    // Get all active templates ordered by step order
    List<ScheduleStepTemplate> findByIsActiveTrueOrderByStepOrderAsc();

    // Filter by Category (PRE_ADMISSION / COUNSELLING)
    List<ScheduleStepTemplate> findByCategoryAndIsActiveTrueOrderByStepOrderAsc(String category);

    // Find template by step order
    Optional<ScheduleStepTemplate> findByStepOrder(Integer stepOrder);

    // Check if a step order already exists
    boolean existsByStepOrder(Integer stepOrder);

    // Find the current highest phase number for a specific route (CUET or NON_CUET)
    @Query("SELECT MAX(t.phaseNumber) FROM ScheduleStepTemplate t WHERE t.isActive = true AND t.admissionRoute = :route")
    Optional<Integer> findMaxPhaseNumberByRoute(@Param("route") String route);

    // Find the absolute highest step order currently in the timeline
    @Query("SELECT MAX(t.stepOrder) FROM ScheduleStepTemplate t")
    Optional<Integer> findAbsoluteMaxStepOrder();

    // Find max order within a specific category
    @Query("SELECT MAX(t.stepOrder) FROM ScheduleStepTemplate t WHERE t.isActive = true AND t.category = :category")
    Optional<Integer> findMaxStepOrderByCategory(@Param("category") String category);

    // Get the maximum active step order
    @Query("SELECT MAX(t.stepOrder) FROM ScheduleStepTemplate t WHERE t.isActive = true")
    Optional<Integer> findMaxStepOrder();

    // Count total active templates
    @Query("SELECT COUNT(t) FROM ScheduleStepTemplate t WHERE t.isActive = true")
    Long countActiveTemplates();

    List<ScheduleStepTemplate> findAllByOrderByStepOrderAsc();

    // Find the max phase for a specific route OR the Combined route
    @Query("SELECT MAX(t.phaseNumber) FROM ScheduleStepTemplate t WHERE t.isActive = true AND t.category = 'COUNSELLING' AND t.admissionRoute IN (:route, 'COMBINED')")
    Optional<Integer> findMaxPhaseNumberByRouteOrCombined(@Param("route") String route);

    // Check if a specific route exists in the active timeline
    boolean existsByAdmissionRouteAndIsActiveTrue(String route);

    // Count how many specific phases this route has had
    @Query("SELECT COUNT(DISTINCT t.phaseNumber) FROM ScheduleStepTemplate t WHERE t.isActive = true AND t.category = 'COUNSELLING' AND t.admissionRoute = :route")
    Long countDistinctPhasesByRoute(@Param("route") String route);

    // Count how many combined phases exist
    @Query("SELECT COUNT(DISTINCT t.phaseNumber) FROM ScheduleStepTemplate t WHERE t.isActive = true AND t.category = 'COUNSELLING' AND t.admissionRoute = 'COMBINED'")
    Long countDistinctCombinedPhases();

    // Keep the global max one for when we generate a brand new COMBINED phase
    @Query("SELECT MAX(t.phaseNumber) FROM ScheduleStepTemplate t WHERE t.isActive = true AND t.category = 'COUNSELLING'")
    Optional<Integer> findGlobalMaxPhaseNumber();
}