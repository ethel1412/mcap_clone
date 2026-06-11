package nic.meg.mcap.repositories;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import nic.meg.mcap.entities.InstituteDepartment;

public interface InstituteDepartmentRepository extends JpaRepository<InstituteDepartment, Integer> {

	// Exists / fetch the unique mapping (Institute × Department)
	boolean existsByInstituteInstituteIdAndDepartmentDepartmentId(Short instituteId, Short departmentId);

	Optional<InstituteDepartment> findByInstituteInstituteIdAndDepartmentDepartmentId(Integer instituteId,
			Integer departmentId);

	List<InstituteDepartment> findByInstituteInstituteIdAndActiveTrueOrderByDepartmentDepartmentNameAsc(
			Short instituteId);

	List<InstituteDepartment> findByInstituteInstituteIdAndDepartmentDepartmentNameContainingIgnoreCaseAndActiveTrue(
			Integer instituteId, String q);

	List<InstituteDepartment> findByDepartmentDepartmentIdAndActiveTrue(Integer departmentId);

	List<InstituteDepartment> findByInstituteInstituteIdInAndActiveTrueOrderByInstituteInstituteIdAsc(
			Collection<Integer> instituteIds);

	Page<InstituteDepartment> findByInstituteInstituteId(Integer instituteId, Pageable pageable);

	List<InstituteDepartment> findByInstituteInstituteIdOrderByDepartmentDepartmentNameAsc(Short instituteId);

	InstituteDepartment findFirstByInstituteInstituteId(Short instituteId);

	List<InstituteDepartment> findByInstitute_InstituteId(Short instituteId);
}
