package nic.meg.mcap.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import nic.meg.mcap.dto.response.InstituteMISDTO;
import nic.meg.mcap.entities.Institute;
import nic.meg.mcap.enums.InstituteStatus;

public interface InstituteRepository extends JpaRepository<Institute, Short> {

	List<Institute> findByStatus(InstituteStatus status);

	long countByStatus(InstituteStatus status);

	List<Institute> findTop5ByOrderByInstituteIdDesc();

	boolean existsByAISHEIdIgnoreCase(String aisheId);

	boolean existsByInstitutionOfficialEmailIdIgnoreCase(String email);

	boolean existsByInstitutionOfficialContactNumber(String contactNumber);

	boolean existsByInstitutionWebsiteIgnoreCase(String website);

	boolean existsByAISHEIdIgnoreCaseAndInstituteIdNot(String aisheId, Short instituteId);

	boolean existsByInstitutionOfficialEmailIdIgnoreCaseAndInstituteIdNot(String email, Short instituteId);

	boolean existsByInstitutionOfficialContactNumberAndInstituteIdNot(String contactNumber, Short instituteId);

	boolean existsByInstitutionWebsiteIgnoreCaseAndInstituteIdNot(String website, Short instituteId);

	Optional<Institute> findByAISHEIdIgnoreCase(String aisheId);

	Optional<Institute> findByInstitutionOfficialEmailIdIgnoreCase(String emailId);

	List<Institute> findTop10ByOrderByInstituteIdDesc();

	Optional<Institute> findByAISHEIdIgnoreCaseAndInstitutionOfficialEmailIdIgnoreCaseAndInstitutionOfficialContactNumber(
			String upperCase, String lowerCase, String trim);

	@Query("""
			SELECT
			    i.instituteId AS instituteId,
			    i.instituteName AS instituteName,

			    COUNT(DISTINCT id.instituteDepartmentId) AS departmentCount,
			    COUNT(DISTINCT po.programme.programmeId) AS programmeCount

			FROM Institute i
			LEFT JOIN InstituteDepartment id ON id.institute = i
			LEFT JOIN ProgrammeOffered po ON po.instituteDepartment = id

			GROUP BY i.instituteId, i.instituteName
			""")
		List<InstituteMISDTO> getMIS();
}
