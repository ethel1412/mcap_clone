package nic.meg.mcap.services;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import nic.meg.mcap.dto.request.ProgrammeOfferedBatchAssignRequestDTO;
import nic.meg.mcap.dto.request.ProgrammeOfferedRequestDTO;
import nic.meg.mcap.dto.response.ProgrammeOfferedResponseDTO;
import nic.meg.mcap.entities.ProgrammeOffered;
import nic.meg.mcap.enums.ProgrammeLevel;

public interface ProgrammeOfferedService {
	List<ProgrammeOfferedResponseDTO> createProgrammeOffered(ProgrammeOfferedRequestDTO requestDTO,
			Short loggedInInstituteId);

	List<ProgrammeOfferedResponseDTO> getProgrammeOfferedById(Integer id);

	ProgrammeOfferedResponseDTO updateProgrammeOffered(Integer id, ProgrammeOfferedRequestDTO requestDTO,
			Short loggedInInstituteId);

	void deleteProgrammeOffered(Integer id, Short loggedInInstituteId);

	List<ProgrammeOfferedResponseDTO> listProgrammesByInstituteDepartment(Short instituteId, Short departmentId);

	void assignMultipleProgrammesToDepartment(ProgrammeOfferedBatchAssignRequestDTO batchRequest,
			Short loggedInInstituteId);

	List<ProgrammeOfferedResponseDTO> getAllProgrammesOffered();

	List<ProgrammeOfferedResponseDTO> getProgrammesOfferedByProgrammeName(String ProgrammeName);

	List<ProgrammeOfferedResponseDTO> listProgrammesByInstitute(Short instituteId);

	List<ProgrammeOfferedResponseDTO> getProgrammesOfferedByInstituteAndStream(Short instituteId, Short streamId);

	ProgrammeOffered findByIdAndInstituteUsername(Integer id, String username);

	Optional<ProgrammeOffered> findById(Integer programmeOfferedId);

	List<String> findDistinctProgrammeLevelsByInstitute(Short instituteId);

	List<ProgrammeOfferedResponseDTO> findProgrammesByLevelAndInstitute(ProgrammeLevel level, Short instituteId);

	List<ProgrammeOffered> getProgrammesForAdmissionWindow(Short admissionWindowId);

	List<ProgrammeOfferedResponseDTO> listProgrammesByInstituteAndProgrammeIds(Short instituteId,
			Collection<Short> programmeIds);

	List<ProgrammeOfferedResponseDTO> findInstitutesByProgramme(Short programmeId);

	Long countByInstitute(Short instituteId);

	void deleteAllShifts(Integer id, Short instituteId);
	
	Map<String, Map<String, Map<String, List<ProgrammeOfferedResponseDTO>>>> getGroupedData();

}