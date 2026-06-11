package nic.meg.mcap.repositories;

import nic.meg.mcap.entities.SubjectMark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubjectMarkRepository extends JpaRepository<SubjectMark, Long> {
}