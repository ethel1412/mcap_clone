package nic.meg.mcap.repositories;

import nic.meg.mcap.entities.Applicant;
import nic.meg.mcap.entities.CuetScore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CuetScoreRepository extends JpaRepository<CuetScore, Long> {
    Optional<CuetScore> findByApplicant(Applicant applicant);
}
