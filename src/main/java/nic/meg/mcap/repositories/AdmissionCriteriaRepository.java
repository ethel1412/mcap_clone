package nic.meg.mcap.repositories;

import nic.meg.mcap.entities.AdmissionCriteria;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AdmissionCriteriaRepository extends JpaRepository<AdmissionCriteria, Long> {

    // For UG: Find criteria by admission window and stream
    Optional<AdmissionCriteria> findByAdmissionWindowAdmissionIdAndStreamStreamId(
            Short admissionWindowId, Short streamId
    );

    // For PG: Find criteria by admission window and programme
    Optional<AdmissionCriteria> findByAdmissionWindowAdmissionIdAndProgrammeProgrammeId(
            Short admissionWindowId, Short programmeId
    );

    // Check if criteria exists for UG stream
    boolean existsByAdmissionWindowAdmissionIdAndStreamStreamId(
            Short admissionWindowId, Short streamId
    );

    // Check if criteria exists for PG programme
    boolean existsByAdmissionWindowAdmissionIdAndProgrammeProgrammeId(
            Short admissionWindowId, Short programmeId
    );

    // Get all criteria entries for an admission window
    List<AdmissionCriteria> findByAdmissionWindowAdmissionId(Short admissionWindowId);

}
