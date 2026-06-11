package nic.meg.mcap.repositories;

import nic.meg.mcap.entities.Applicant;
import nic.meg.mcap.entities.JeeScore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JeeScoreRepository extends JpaRepository<JeeScore, Long> {
    Optional<JeeScore> findByApplicant(Applicant applicant);
}
