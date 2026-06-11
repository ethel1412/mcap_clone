package nic.meg.mcap.repositories;

import nic.meg.mcap.entities.ProgrammeRequest;
import nic.meg.mcap.enums.ApprovalStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProgrammeRequestRepository extends JpaRepository<ProgrammeRequest, Long> {
    
    // To show the Institute their specific requests
    List<ProgrammeRequest> findByInstitute_InstituteId(Short instituteId);
    
    // To show the Controller all PENDING requests
    List<ProgrammeRequest> findByStatus(String status);
    
    List<ProgrammeRequest> findAllByOrderByCreatedAtDesc();
    
    //Count for Institute (My Pending Requests)
    long countByInstitute_InstituteIdAndStatus(Short instituteId, String status);
    //Count for Controller (All Pending Requests)
    long countByStatus(String status);

    boolean existsByInstitute_InstituteIdAndProgrammeNameIgnoreCaseAndStatus(
        Short instituteId, 
        String programmeName, 
        String status
    );
    // To check for duplicates when submitting new request
    List<ProgrammeRequest> findByInstitute_InstituteIdAndStatus(Short instituteId, String status);
}