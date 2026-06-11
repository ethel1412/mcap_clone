package nic.meg.mcap.repositories;

import nic.meg.mcap.entities.InstituteAdmissionPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Set;

public interface InstituteAdmissionPreferenceRepository extends JpaRepository<InstituteAdmissionPreference, Long> {

    Optional<InstituteAdmissionPreference> findByInstituteInstituteIdAndAdmissionWindowAdmissionId(
            Short instituteId, Short admissionWindowId);

    /**
     * Returns the set of institute IDs that have opted for the given CUET preference
     * for a specific admission window. Used by the allotment engine to gate eligibility.
     */
    @Query("SELECT p.institute.instituteId FROM InstituteAdmissionPreference p " +
            "WHERE p.admissionWindow.admissionId = :admissionWindowId " +
            "AND p.wantsCuet = :wantsCuet")
    Set<Short> findInstituteIdsByWindowAndCuetPreference(
            @Param("admissionWindowId") Short admissionWindowId,
            @Param("wantsCuet") boolean wantsCuet);
}