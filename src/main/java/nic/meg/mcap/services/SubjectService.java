package nic.meg.mcap.services;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import nic.meg.mcap.dto.response.SubjectResponseDTO;
import nic.meg.mcap.enums.SubjectType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import nic.meg.mcap.entities.Subject;

public interface SubjectService {

    List<Subject> getAllSubjects();

    Page<Subject> getAllSubjects(Pageable pageable);

    Optional<Subject> getSubjectById(Integer subjectId);

    Subject createSubject(Subject subject);

    Subject updateSubject(Integer subjectId, Subject subject);

    void deleteSubject(Integer subjectId);

    boolean existsBySubjectName(String subjectName);

    boolean existsBySubjectNameAndIdNot(String subjectName, Integer subjectId);

    List<Subject> searchSubjects(String query);

    Page<Subject> searchSubjects(String query, Pageable pageable);

    List<Subject> searchSubjectsByName(String searchTerm);

    Subject findById(Integer id);

    List<Subject> findByStream(Short streamId);

    List<Subject> findBySubjectType(SubjectType subjectType);

    Map<SubjectType, List<Subject>> getAllSubjectsGroupedByType();
}