package nic.meg.mcap.services.impl;

import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.stream.Collectors;

import nic.meg.mcap.enums.SubjectType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import nic.meg.mcap.entities.Subject;
import nic.meg.mcap.dto.response.SubjectResponseDTO;
import nic.meg.mcap.repositories.SubjectRepository;
import nic.meg.mcap.services.SubjectService;

@Service
public class SubjectServiceImpl implements SubjectService {

    @Autowired
    private SubjectRepository subjectRepository;

    @Override
    public List<Subject> getAllSubjects() {
        return subjectRepository.findAll(Sort.by("subjectName"));
    }

    @Override
    public Page<Subject> getAllSubjects(Pageable pageable) {
        return subjectRepository.findAll(pageable);
    }

    @Override
    public Optional<Subject> getSubjectById(Integer subjectId) {
        return subjectRepository.findById(subjectId);
    }

    @Override
    @Transactional
    public Subject createSubject(Subject subject) {
        // Check for duplicate subject name
        if (existsBySubjectName(subject.getSubjectName())) {
            throw new IllegalStateException("Subject already exists with name: " + subject.getSubjectName());
        }

        return subjectRepository.save(subject);
    }

    @Override
    @Transactional
    public Subject updateSubject(Integer subjectId, Subject subject) {
        Subject existingSubject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new EntityNotFoundException("Subject not found with ID: " + subjectId));

        // Check for duplicate subject name (excluding current subject)
        if (existsBySubjectNameAndIdNot(subject.getSubjectName(), subjectId)) {
            throw new IllegalStateException("Subject already exists with name: " + subject.getSubjectName());
        }

        existingSubject.setSubjectName(subject.getSubjectName());
        existingSubject.setSubjectCode(subject.getSubjectCode());

        return subjectRepository.save(existingSubject);
    }

    @Override
    @Transactional
    public void deleteSubject(Integer subjectId) {
        if (!subjectRepository.existsById(subjectId)) {
            throw new EntityNotFoundException("Cannot delete. Subject not found with ID: " + subjectId);
        }

        subjectRepository.deleteById(subjectId);
    }

    @Override
    public boolean existsBySubjectName(String subjectName) {
        return subjectRepository.existsBySubjectNameIgnoreCase(subjectName);
    }

    @Override
    public boolean existsBySubjectNameAndIdNot(String subjectName, Integer subjectId) {
        Optional<Subject> existing = subjectRepository.findBySubjectNameIgnoreCase(subjectName);
        return existing.isPresent() && !existing.get().getSubjectId().equals(subjectId);
    }

    @Override
    public List<Subject> searchSubjects(String query) {
        return subjectRepository.findBySubjectNameContainingIgnoreCaseOrderBySubjectNameAsc(query);
    }

    @Override
    public Page<Subject> searchSubjects(String query, Pageable pageable) {
        return subjectRepository.findBySubjectNameContainingIgnoreCaseOrSubjectCodeContainingIgnoreCase(
                query, query, pageable);
    }

    @Override
    public List<Subject> searchSubjectsByName(String searchTerm) {
        return subjectRepository.findBySubjectNameContainingIgnoreCase(searchTerm);
    }

    @Override
    public Subject findById(Integer id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Subject not found"));
    }

    @Override
    public List<Subject> findByStream(Short streamId) {
        return subjectRepository.findByStreams_StreamIdOrderBySubjectNameAsc(streamId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Subject> findBySubjectType(SubjectType subjectType) {
        return subjectRepository.findBySubjectType(subjectType);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<SubjectType, List<Subject>> getAllSubjectsGroupedByType() {
        return subjectRepository.findAll().stream()
                .filter(subject -> subject.getSubjectType() != null)
                .collect(Collectors.groupingBy(Subject::getSubjectType));
    }

    private SubjectResponseDTO convertToDto(Subject subject) {
        SubjectResponseDTO dto = new SubjectResponseDTO();
        dto.setSubjectId(subject.getSubjectId());
        dto.setSubjectName(subject.getSubjectName());
        dto.setSubjectCode(subject.getSubjectCode());
        return dto;
    }
}