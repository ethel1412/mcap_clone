package nic.meg.mcap.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import nic.meg.mcap.dto.response.AllProgrammeResponseDTO;
import nic.meg.mcap.entities.Programme;
import nic.meg.mcap.enums.ProgrammeLevel;

public interface ProgrammeRepository extends JpaRepository<Programme, Short> {
	List<Programme> findByProgrammeName(String programmeName);

	List<Programme> findByStreamStreamId(Short streamId);

	List<Programme> findByProgrammeLevel(String programmeLevel);

	List<Programme> findByProgrammeNameContainingIgnoreCase(String query);

	List<Programme> findByStreamStreamIdAndProgrammeLevel(Short streamId, ProgrammeLevel programmeLevel);

	Optional<Programme> findByProgrammeNameAndProgrammeLevel(String programmeName, ProgrammeLevel programmeLevel);

	boolean existsByProgrammeNameIgnoreCase(String programmeName);

	List<Programme> findByDepartment_DepartmentId(Short departmentId);

	@Query("""
			    SELECT new nic.meg.mcap.dto.response.AllProgrammeResponseDTO(
			        p.programmeId,
			        p.programmeName,
			        p.programmeLevel,
			        p.stream
			    )
			    FROM Programme p
			""")
	List<AllProgrammeResponseDTO> getAllProgrammes();

	List<Programme> findByStreamStreamIdIn(List<Short> defaultStreamIds);
}
