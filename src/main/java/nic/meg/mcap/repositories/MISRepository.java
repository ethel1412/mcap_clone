//package nic.meg.mcap.repositories;
//
//import java.util.List;
//
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//
//import nic.meg.mcap.dto.request.InstituteMISDTO;
//import nic.meg.mcap.entities.AcademicRecord;
//
//public interface MISRepository extends JpaRepository<AcademicRecord, Long> {
//	@Query("""
//			SELECT
//			    i.instituteName as instituteName,
//			    i.AISHEId as AISHEId,
//			    COUNT(DISTINCT id.instituteDepartmentId) as departmentCount,
//			    COUNT(DISTINCT po.programmeOfferedId) as programmeCount
//			FROM Institute i
//			LEFT JOIN InstituteDepartment id ON id.institute = i
//			LEFT JOIN ProgrammeOffered po ON po.instituteDepartment = id
//			GROUP BY i.instituteId, i.instituteName, i.AISHEId
//			""")
//	List<InstituteMISDTO> getMIS();
//}
