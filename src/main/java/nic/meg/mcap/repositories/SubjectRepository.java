package nic.meg.mcap.repositories;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import nic.meg.mcap.entities.CuetPaper;
import nic.meg.mcap.entities.Subject;
import nic.meg.mcap.enums.SubjectType;

public interface SubjectRepository extends JpaRepository<Subject, Integer> {

	Optional<Subject> findBySubjectNameIgnoreCase(String subjectName);

	boolean existsBySubjectNameIgnoreCase(String subjectName);

	List<Subject> findBySubjectCode(String subjectCode);

	List<Subject> findBySubjectNameContainingIgnoreCaseOrderBySubjectNameAsc(String q);

	Page<Subject> findBySubjectNameContainingIgnoreCaseOrSubjectCodeContainingIgnoreCase(String nameQuery,
			String codeQuery, Pageable pageable);
    List<Subject> findBySubjectNameContainingIgnoreCase(String searchTerm);

    List<Subject> findByStreams_StreamIdOrderBySubjectNameAsc(Short streamId);

    List<Subject> findBySubjectType(SubjectType subjectType);

	List<Subject> findBySubjectTypeOrderBySubjectName(SubjectType general);

	List<Subject> findBySubjectType(String subjectType);
}
