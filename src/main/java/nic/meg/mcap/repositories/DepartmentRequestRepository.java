package nic.meg.mcap.repositories;

import nic.meg.mcap.entities.DepartmentRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepartmentRequestRepository extends JpaRepository<DepartmentRequest, Long> {

    // For Institute: View their own requests
    List<DepartmentRequest> findByInstitute_InstituteIdOrderByCreatedAtDesc(Short instituteId);

    // For Controller: View all pending requests
    List<DepartmentRequest> findByStatusOrderByCreatedAtDesc(String status);

    // For Controller: View ALL requests (History)
    List<DepartmentRequest> findAllByOrderByCreatedAtDesc();

    // Counts for Badges
    long countByInstitute_InstituteIdAndStatus(Short instituteId, String status);
    long countByStatus(String status);
    //  Check for duplicates when submitting new request
    List<DepartmentRequest> findByInstitute_InstituteIdAndStatus(Short instituteId, String status);
}