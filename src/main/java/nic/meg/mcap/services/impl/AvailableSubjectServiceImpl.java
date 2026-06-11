package nic.meg.mcap.services.impl;

import jakarta.persistence.EntityNotFoundException;
import nic.meg.mcap.dto.request.AvailableSubjectsRequestDTO;
import nic.meg.mcap.dto.response.SubjectResponseDTO;
import nic.meg.mcap.entities.AvailableSubject;
import nic.meg.mcap.entities.ProgrammeOffered;
import nic.meg.mcap.entities.Subject;
import nic.meg.mcap.enums.Shift;
import nic.meg.mcap.enums.SubjectType;
import nic.meg.mcap.repositories.AvailableSubjectRepository;
import nic.meg.mcap.repositories.ProgrammesOfferedRepository;
import nic.meg.mcap.repositories.SubjectRepository;
import nic.meg.mcap.services.AvailableSubjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AvailableSubjectServiceImpl implements AvailableSubjectService {

    private static final Logger logger = LoggerFactory.getLogger(AvailableSubjectServiceImpl.class);

    @Autowired private AvailableSubjectRepository availableSubjectRepository;
    @Autowired private ProgrammesOfferedRepository programmeOfferedRepository;
    @Autowired private SubjectRepository subjectRepository;

    @Override
    @Transactional
    public void saveAvailableSubjects(AvailableSubjectsRequestDTO dto) {
       ProgrammeOffered po = programmeOfferedRepository.findById(dto.getProgrammeOfferedId())
                .orElseThrow(() -> new EntityNotFoundException("Programme Offered not found with ID: " + dto.getProgrammeOfferedId()));

        availableSubjectRepository.deleteByProgrammeOfferedAndShift(po, dto.getShift());
       
        List<AvailableSubject> subjectsToSave = new ArrayList<>();

        addSubjectsToList(subjectsToSave, po, dto.getShift(), SubjectType.MINOR, dto.getMinorSubjectIds());
        addSubjectsToList(subjectsToSave, po, dto.getShift(), SubjectType.MDC, dto.getMdcSubjectIds());
        addSubjectsToList(subjectsToSave, po, dto.getShift(), SubjectType.AEC, dto.getAecSubjectIds());
        addSubjectsToList(subjectsToSave, po, dto.getShift(), SubjectType.SEC, dto.getSecSubjectIds());
        addSubjectsToList(subjectsToSave, po, dto.getShift(), SubjectType.VAC, dto.getVacSubjectIds());

        availableSubjectRepository.saveAll(subjectsToSave);
    }

    private void addSubjectsToList(List<AvailableSubject> list, ProgrammeOffered po, Shift shift, SubjectType type, List<Integer> subjectIds) {
        if (subjectIds == null || subjectIds.isEmpty()) {
            return;
        }
        List<Subject> subjects = subjectRepository.findAllById(subjectIds);
        for (Subject subject : subjects) {
            AvailableSubject as = new AvailableSubject();
            as.setProgrammeOffered(po);
            as.setShift(shift);
            as.setSubjectType(type);
            as.setSubject(subject);
            list.add(as);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Shift, Map<SubjectType, List<SubjectResponseDTO>>> getAvailableSubjectsGroupedByShift(Integer programmeOfferedId) {
        ProgrammeOffered po = programmeOfferedRepository.getReferenceById(programmeOfferedId);
        List<AvailableSubject> availableSubjects = availableSubjectRepository.findByProgrammeOffered(po);

        return availableSubjects.stream()
                .collect(Collectors.groupingBy(AvailableSubject::getShift,
                        Collectors.groupingBy(AvailableSubject::getSubjectType,
                                Collectors.mapping(as -> convertToDto(as.getSubject()), Collectors.toList()))));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<SubjectType, List<SubjectResponseDTO>> getAvailableSubjectsForShift(Integer programmeOfferedId, Shift shift) {
        ProgrammeOffered po = programmeOfferedRepository.getReferenceById(programmeOfferedId);
        List<AvailableSubject> availableSubjects = availableSubjectRepository.findByProgrammeOfferedAndShift(po, shift);

        return availableSubjects.stream()
                .collect(Collectors.groupingBy(AvailableSubject::getSubjectType,
                        Collectors.mapping(as -> convertToDto(as.getSubject()), Collectors.toList())));
    }

    /**
     * Converts a Subject entity to a SubjectResponseDTO.
     * This now uses the correct getter method from your Subject entity.
     */
    private SubjectResponseDTO convertToDto(Subject subject) {
        if (subject == null) return null;
        SubjectResponseDTO dto = new SubjectResponseDTO();
        dto.setSubjectId(subject.getSubjectId());
        dto.setSubjectName(subject.getSubjectName());
        dto.setSubjectCode(subject.getSubjectCode());
        return dto;
    }
}