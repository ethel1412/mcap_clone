package nic.meg.mcap.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import nic.meg.mcap.entities.SubjectCombinationHeader;

public interface SubjectCombinationHeaderRepository extends JpaRepository<SubjectCombinationHeader, Long> {
	List<SubjectCombinationHeader> findByProgrammeProgrammeIdAndActiveTrueOrderByCombinationNameAsc(Integer programmeId);

	Optional<SubjectCombinationHeader> findByProgrammeProgrammeIdAndCombinationNameIgnoreCase(Integer programmeId,
			String combinationName);

	boolean existsByProgrammeProgrammeIdAndCombinationNameIgnoreCase(Integer programmeId, String combinationName);

	// optional search
	Page<SubjectCombinationHeader> findByProgrammeProgrammeIdAndCombinationNameContainingIgnoreCase(Integer programmeId,
			String q, Pageable pageable);
}
