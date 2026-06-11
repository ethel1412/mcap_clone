package nic.meg.mcap.repositories;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import nic.meg.mcap.entities.Department;

public interface DepartmentRepository extends JpaRepository<Department, Short> {

	Optional<Department> findByDepartmentNameIgnoreCase(String departmentName);

	boolean existsByDepartmentNameIgnoreCase(String departmentName);
	boolean existsByDepartmentCodeIgnoreCase(String departmentCode);

	List<Department> findByDepartmentCode(String departmentCode);

	List<Department> findByDepartmentNameContainingIgnoreCaseOrderByDepartmentNameAsc(String q);

	Page<Department> findByDepartmentNameContainingIgnoreCaseOrDepartmentCodeContainingIgnoreCase(String nameQuery,
			String codeQuery, Pageable pageable);

	List<Department> findAllByOrderByDepartmentNameAsc();

	List<Department> findByDepartmentIdIn(Collection<Short> ids);
}
