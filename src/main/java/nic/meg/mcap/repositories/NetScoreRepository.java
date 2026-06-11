package nic.meg.mcap.repositories;

import nic.meg.mcap.entities.Applicant;
import nic.meg.mcap.entities.NetScore;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface NetScoreRepository extends JpaRepository<NetScore, Long> {
    Optional<NetScore> findByApplicant(Applicant applicant);
}