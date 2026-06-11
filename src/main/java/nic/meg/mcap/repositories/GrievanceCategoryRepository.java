package nic.meg.mcap.repositories;

import nic.meg.mcap.entities.GrievanceCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface GrievanceCategoryRepository extends JpaRepository<GrievanceCategory, Long> {
    Optional<GrievanceCategory> findByCode(String code);
}