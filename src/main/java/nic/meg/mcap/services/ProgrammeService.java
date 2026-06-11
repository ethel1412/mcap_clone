package nic.meg.mcap.services;

import java.util.List;
import nic.meg.mcap.dto.request.ProgrammeRequestDTO;
import nic.meg.mcap.entities.Programme;

public interface ProgrammeService {

    List<Programme> getAllProgrammes();
    
    Programme getProgrammeById(Short id);
    
    Programme saveProgramme(ProgrammeRequestDTO ProgrammeDTO);
    
    Programme updateProgramme(Short id, ProgrammeRequestDTO ProgrammeDTO);
    
    void deleteProgramme(Short id);

    List<Programme> getProgrammesByName(String name);
    List<Programme> getProgrammesByStreamId(Short streamId);
    List<Programme> getProgrammesByLevel(String level);
    List<Programme> searchProgrammes(String query);

	List<Programme> getProgrammesByDepartmentId(Integer deptId);

}