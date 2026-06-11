package nic.meg.mcap.repositories;

import nic.meg.mcap.entities.EligibilityCriteria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EligibilityCriteriaRepository extends JpaRepository<EligibilityCriteria, Short> {

    // Common eligibility within a window per programme
    Optional<EligibilityCriteria> findByAdmissionWindowAdmissionCodeAndProgrammeProgrammeId(
            String admissionWindowCode,
            Short programmeId
    );
}