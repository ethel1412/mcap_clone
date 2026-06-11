package nic.meg.mcap.services.impl;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import nic.meg.mcap.entities.ProgrammeOffered;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import nic.meg.mcap.dto.request.SemesterRequestDTO;
import nic.meg.mcap.dto.response.SemesterResponseDTO;
import nic.meg.mcap.dto.response.SubjectResponseDTO;
import nic.meg.mcap.entities.Semester;
import nic.meg.mcap.entities.Subject;
import nic.meg.mcap.repositories.ProgrammesOfferedRepository;
import nic.meg.mcap.repositories.SemesterRepository;
import nic.meg.mcap.repositories.SubjectAssignmentRepository;
import nic.meg.mcap.repositories.SubjectRepository;
import nic.meg.mcap.services.SemesterService;

@Service
public class SemesterServiceImpl implements SemesterService {

    @Autowired
    private SemesterRepository semesterRepository;

    @Autowired
    private ProgrammesOfferedRepository programmesOfferedRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private SubjectAssignmentRepository subjectAssignmentRepository;

    @Override
    public List<SemesterResponseDTO> getSemestersByProgramme(Integer ProgrammeOfferedId) {
        List<Semester> semesters = semesterRepository.findByProgrammeOfferedProgrammeOfferedIdOrderBySemesterNumberAsc(ProgrammeOfferedId);
        return semesters.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SemesterResponseDTO createSemester(SemesterRequestDTO requestDTO) {
        ProgrammeOffered programmeOffered = programmesOfferedRepository.findById(requestDTO.getProgrammeOfferedId())
                .orElseThrow(() -> new EntityNotFoundException("ProgrammeOffered not found with ID: " + requestDTO.getProgrammeOfferedId()));

        // Check if semester number already exists for this Programme
        if (semesterRepository.existsByProgrammeOfferedProgrammeOfferedIdAndSemesterNumber(
                requestDTO.getProgrammeOfferedId(), requestDTO.getSemesterNumber())) {
            throw new IllegalStateException("Semester " + requestDTO.getSemesterNumber() +
                    " already exists for this Programme");
        }

        Semester semester = new Semester();
        semester.setProgrammeOffered(programmeOffered);
        semester.setSemesterNumber(requestDTO.getSemesterNumber());

        // Set semester name or default to "Semester [Number]"
        if (requestDTO.getSemesterName() != null && !requestDTO.getSemesterName().trim().isEmpty()) {
            semester.setSemesterName(requestDTO.getSemesterName().trim());
        } else {
            semester.setSemesterName("Semester " + requestDTO.getSemesterNumber());
        }

        semester.setActive(requestDTO.isActive());

        Semester saved = semesterRepository.save(semester);
        return convertToDTO(saved);
    }

    @Override
    @Transactional
    public SemesterResponseDTO updateSemester(Long semesterId, SemesterRequestDTO requestDTO) {
        Semester existingSemester = semesterRepository.findById(semesterId)
                .orElseThrow(() -> new EntityNotFoundException("Semester not found with ID: " + semesterId));

        ProgrammeOffered programmeOffered = programmesOfferedRepository.findById(requestDTO.getProgrammeOfferedId())
                .orElseThrow(() -> new EntityNotFoundException("ProgrammeOffered not found with ID: " + requestDTO.getProgrammeOfferedId()));

        // Check if semester number already exists for this Programme (excluding current)
        Semester existingWithSameNumber = semesterRepository
                .findByProgrammeOfferedProgrammeOfferedIdAndSemesterNumber(requestDTO.getProgrammeOfferedId(), requestDTO.getSemesterNumber())
                .orElse(null);

        if (existingWithSameNumber != null && !existingWithSameNumber.getSemesterId().equals(semesterId)) {
            throw new IllegalStateException("Semester " + requestDTO.getSemesterNumber() +
                    " already exists for this Programme");
        }

        existingSemester.setProgrammeOffered(programmeOffered);
        existingSemester.setSemesterNumber(requestDTO.getSemesterNumber());

        if (requestDTO.getSemesterName() != null && !requestDTO.getSemesterName().trim().isEmpty()) {
            existingSemester.setSemesterName(requestDTO.getSemesterName().trim());
        } else {
            existingSemester.setSemesterName("Semester " + requestDTO.getSemesterNumber());
        }

        existingSemester.setActive(requestDTO.isActive());

        Semester updated = semesterRepository.save(existingSemester);
        return convertToDTO(updated);
    }

    @Override
    @Transactional
    public void deleteSemester(Long semesterId) {
        if (!semesterRepository.existsById(semesterId)) {
            throw new EntityNotFoundException("Semester not found with ID: " + semesterId);
        }

        // Delete all subject assignments for this semester first
        subjectAssignmentRepository.deleteBySemesterSemesterId(semesterId);

        // Then delete the semester
        semesterRepository.deleteById(semesterId);
    }

    @Override
    public List<SubjectResponseDTO> getAvailableSubjectsForSemester(Long semesterId) {
        Semester semester = semesterRepository.findById(semesterId)
                .orElseThrow(() -> new EntityNotFoundException("Semester not found with ID: " + semesterId));

        // Get all subjects
        List<Subject> allSubjects = subjectRepository.findAll();

        // Get subjects already assigned to this semester
        Set<Integer> assignedSubjectIds = subjectAssignmentRepository.findAssignedSubjectIdsBySemesterId(semesterId);

        // Filter out already assigned subjects
        List<Subject> availableSubjects = allSubjects.stream()
                .filter(subject -> !assignedSubjectIds.contains(subject.getSubjectId()))
                .collect(Collectors.toList());

        return availableSubjects.stream()
                .map(this::convertSubjectToDTO)
                .collect(Collectors.toList());
    }

    private SemesterResponseDTO convertToDTO(Semester entity) {
        SemesterResponseDTO dto = new SemesterResponseDTO();
        dto.setSemesterId(entity.getSemesterId());
        dto.setSemesterNumber(entity.getSemesterNumber());
        dto.setSemesterName(entity.getSemesterName());
        dto.setProgrammeOfferedId(entity.getProgrammeOffered().getProgrammeOfferedId());
        dto.setProgrammeName(entity.getProgrammeOffered().getProgramme().getProgrammeName());
        dto.setDepartmentName(entity.getProgrammeOffered().getInstituteDepartment().getDepartment().getDepartmentName());
        dto.setActive(entity.isActive());

        // Count assigned subjects
        Long totalSubjects = semesterRepository.countAssignedSubjects(entity.getSemesterId());
        dto.setTotalSubjects(totalSubjects.intValue());

        return dto;
    }

    private SubjectResponseDTO convertSubjectToDTO(Subject entity) {
        SubjectResponseDTO dto = new SubjectResponseDTO();
        dto.setSubjectId(entity.getSubjectId());
        dto.setSubjectName(entity.getSubjectName());
        dto.setSubjectCode(entity.getSubjectCode());
        return dto;
    }
}
