package nic.meg.mcap.repositories;

import nic.meg.mcap.entities.SubjectRequirement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubjectRequirementRepository extends JpaRepository<SubjectRequirement, Long> {
}
