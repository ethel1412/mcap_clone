package nic.meg.mcap.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import nic.meg.mcap.entities.SequenceGenerator;

public interface SequenceGeneratorRepository extends JpaRepository<SequenceGenerator, String> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT s FROM SequenceGenerator s WHERE s.admissionWindow.admissionId = :windowId")
	Optional<SequenceGenerator> findByWindowIdWithLock(@Param("windowId") short windowId);

}