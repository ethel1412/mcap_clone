package nic.meg.mcap.repositories;

import nic.meg.mcap.entities.SeatMatrix;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SeatMatrixRepository extends JpaRepository<SeatMatrix, Long> {

    Optional<SeatMatrix> findByProgrammeOfferedProgrammeOfferedId(Integer programmeOfferedId);

    List<SeatMatrix> findAllByProgrammeOfferedProgrammeOfferedIdIn(List<Integer> programmeOfferedIds);

    @Query("SELECT sm FROM SeatMatrix sm WHERE sm.admissionWindow.admissionId = :admissionWindowId")
    List<SeatMatrix> findByAdmissionWindowId(@Param("admissionWindowId") Short admissionWindowId);

    List<SeatMatrix> findByAdmissionWindow_AdmissionIdAndProgrammeOffered_ProgrammeOfferedIdIn(
            Short admissionId,
            List<Integer> programmeOfferedIds
    );

    // --- OPTIMIZED DASHBOARD QUERIES ---

    @EntityGraph(attributePaths = {
            "programmeOffered",
            "programmeOffered.programme",
            "programmeOffered.programme.stream",
            "programmeOffered.instituteDepartment",
            "programmeOffered.instituteDepartment.institute"
    })
    List<SeatMatrix> findByApprovalStatus(String status);

    @EntityGraph(attributePaths = {
            "programmeOffered",
            "programmeOffered.programme",
            "programmeOffered.programme.stream",
            "programmeOffered.instituteDepartment",
            "programmeOffered.instituteDepartment.institute"
    })
    List<SeatMatrix> findByApprovalStatusIn(List<String> statuses);
    long countByApprovalStatus(String status);

    @Query("SELECT COUNT(sm) FROM SeatMatrix sm WHERE sm.approvalStatus = :status AND sm.admissionWindow.admissionId = :admissionId")
    long countByApprovalStatusAndAdmissionId(@Param("status") String status, @Param("admissionId") Short admissionId);
}