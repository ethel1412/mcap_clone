package nic.meg.mcap.services.impl;

import java.util.List;
import java.util.Optional;

import nic.meg.mcap.entities.InstituteDepartment;
import nic.meg.mcap.entities.Programme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityNotFoundException;
import nic.meg.mcap.dto.request.ProgrammeRequestDTO;
import nic.meg.mcap.entities.Stream;
import nic.meg.mcap.repositories.InstituteDepartmentRepository;
import nic.meg.mcap.repositories.ProgrammeRepository;
import nic.meg.mcap.repositories.StreamRepository;
import nic.meg.mcap.services.ProgrammeService;

@Service
public class ProgrammeServiceImpl implements ProgrammeService {

    @Autowired
    private ProgrammeRepository programmeRepository;
    
    @Autowired
    private StreamRepository streamRepository;
    
    @Autowired
    private InstituteDepartmentRepository instdeptRepository;

    @Override
    public List<Programme> getAllProgrammes() {
        return programmeRepository.findAll(Sort.by("ProgrammeName"));
    }

    @Override
    public Programme getProgrammeById(Short id) {
        return programmeRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Programme not found with ID: " + id));
    }

    @Override
    public Programme saveProgramme(ProgrammeRequestDTO ProgrammeDTO) {
        Stream stream = streamRepository.findById(ProgrammeDTO.getStreamId())
            .orElseThrow(() -> new EntityNotFoundException("Stream not found with ID: " + ProgrammeDTO.getStreamId()));
            
        Programme programme = new Programme();
        programme.setProgrammeName(ProgrammeDTO.getProgrammeName());
        programme.setStream(stream);
        programme.setProgrammeLevel(ProgrammeDTO.getProgrammeLevel());
        
        return programmeRepository.save(programme);
    }

    @Override
    public Programme updateProgramme(Short id, ProgrammeRequestDTO ProgrammeDTO) {
        Programme existingProgramme = getProgrammeById(id);
        
        Stream stream = streamRepository.findById(ProgrammeDTO.getStreamId())
            .orElseThrow(() -> new EntityNotFoundException("Stream not found with ID: " + ProgrammeDTO.getStreamId()));
            
        existingProgramme.setProgrammeName(ProgrammeDTO.getProgrammeName());
        existingProgramme.setStream(stream);
        existingProgramme.setProgrammeLevel(ProgrammeDTO.getProgrammeLevel());
        
        return programmeRepository.save(existingProgramme);
    }

    @Override
    public void deleteProgramme(Short id) {
        if (!programmeRepository.existsById(id)) {
            throw new EntityNotFoundException("Cannot delete. Programme not found with ID: " + id);
        }
        programmeRepository.deleteById(id);
    }

    @Override
    public List<Programme> getProgrammesByName(String name) {
        return programmeRepository.findByProgrammeName(name);
    }

    @Override
    public List<Programme> getProgrammesByStreamId(Short streamId) {
        return programmeRepository.findByStreamStreamId(streamId);
    }
    @Override
    public List<Programme> getProgrammesByLevel(String level) {
        return programmeRepository.findByProgrammeLevel(level);
    }

    @Override
    public List<Programme> searchProgrammes(String query) {
        return programmeRepository.findByProgrammeNameContainingIgnoreCase(query);
    }

	@Override
	public List<Programme> getProgrammesByDepartmentId(Integer deptId) {
		InstituteDepartment instdept = instdeptRepository.findById(deptId)
		        .orElseThrow(() -> new IllegalArgumentException("Invalid departmentId"));
		return programmeRepository.findByDepartment_DepartmentId(instdept.getDepartment().getDepartmentId());
	}

}