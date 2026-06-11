package nic.meg.mcap.repositories;

import nic.meg.mcap.entities.Qualification;
import nic.meg.mcap.enums.QualificationLevel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QualificationRepository extends JpaRepository<Qualification, Long> {
    List<Qualification> findByIsActiveTrueOrderByNameAsc();
    // Find qualifications by levels
    @Query("SELECT q.name FROM Qualification q WHERE q.level IN :levels AND q.isActive = true")
    List<String> findNamesByLevels(@Param("levels") List<QualificationLevel> levels);

}