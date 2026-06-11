package nic.meg.mcap.repositories;

import nic.meg.mcap.entities.AdmissionWindow;
import nic.meg.mcap.entities.Applicant;
import nic.meg.mcap.entities.SeatAllotment;
import nic.meg.mcap.enums.AllotmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface SeatAllotmentRepository extends JpaRepository<SeatAllotment, Long> {

    void deleteByAdmissionWindowAdmissionId(Short admissionId);

    @Query("""
          SELECT sa FROM SeatAllotment sa
          LEFT JOIN FETCH sa.programmeOffered po
          LEFT JOIN FETCH po.programme p
          LEFT JOIN FETCH po.instituteDepartment id
          LEFT JOIN FETCH id.institute i
          WHERE sa.applicant = :applicant
            AND sa.admissionWindow = :window
          """)
    Optional<SeatAllotment> findByApplicantAndAdmissionWindowWithDetails(@Param("applicant") Applicant applicant,
                                                                         @Param("window") AdmissionWindow window);

    // -------------------------
    // DRILL-DOWN DASHBOARD
    // -------------------------
    int countByProgrammeOfferedProgrammeOfferedId(Integer programmeOfferedId);

    long countByProgrammeOfferedProgrammeOfferedIdAndStatus(Integer programmeOfferedId, AllotmentStatus status);

    List<SeatAllotment> findByProgrammeOfferedProgrammeOfferedIdAndStatus(Integer programmeOfferedId, AllotmentStatus status);

    // -------------------------
    // Rounds + phases
    // -------------------------
    void deleteByAdmissionWindowAdmissionIdAndRoundTypeAndPhaseNo(Short admissionId, String roundType, Integer phaseNo);

    List<SeatAllotment> findByApplicantAndAdmissionWindowAdmissionIdOrderByIdDesc(Applicant applicant, Short admissionId);

    List<SeatAllotment> findByAdmissionWindowAdmissionIdAndRoundTypeAndPhaseNoAndProgrammeOfferedProgrammeOfferedId(
            Short admissionId, String roundType, Integer phaseNo, Integer programmeOfferedId);

    int countByAdmissionWindowAdmissionIdAndRoundTypeAndPhaseNoAndProgrammeOfferedProgrammeOfferedId(Short admissionId,
                                                                                                     String roundType, Integer phaseNo, Integer programmeOfferedId);

    long countByAdmissionWindowAdmissionIdAndRoundTypeAndPhaseNoAndStatus(Short admissionId, String roundType,
                                                                          Integer phaseNo, AllotmentStatus status);

    long countByAdmissionWindowAdmissionIdAndRoundTypeAndPhaseNo(Short admissionId, String roundType, Integer phaseNo);

    boolean existsByAdmissionWindowAdmissionIdAndRoundTypeAndPhaseNoLessThanAndApplicationApplicationIdAndStatus(
            Short admissionId, String roundType, Integer phaseNo, Long applicationId, AllotmentStatus status);

    @Query("""
          SELECT sa FROM SeatAllotment sa
          LEFT JOIN FETCH sa.programmeOffered po
          LEFT JOIN FETCH po.programme p
          LEFT JOIN FETCH po.instituteDepartment id
          LEFT JOIN FETCH id.institute i
          WHERE sa.applicant = :applicant
            AND sa.admissionWindow = :window
            AND sa.roundType = :roundType
            AND sa.phaseNo = :phaseNo
          """)
    Optional<SeatAllotment> findByApplicantAndWindowAndRoundPhaseWithDetails(@Param("applicant") Applicant applicant,
                                                                             @Param("window") AdmissionWindow window, @Param("roundType") String roundType,
                                                                             @Param("phaseNo") Integer phaseNo);

    List<SeatAllotment> findByApplicant(Applicant applicant);

    @Query("""
    	      SELECT sa FROM SeatAllotment sa
    	      LEFT JOIN FETCH sa.programmeOffered po
    	      LEFT JOIN FETCH po.programme p
    	      LEFT JOIN FETCH po.instituteDepartment id
    	      LEFT JOIN FETCH id.institute i
    	      WHERE sa.id = :allotmentId
    	      """)
    	Optional<SeatAllotment> findByIdWithDetails(@Param("allotmentId") Long allotmentId);

    @Query("""
          SELECT sa FROM SeatAllotment sa
          JOIN FETCH sa.applicant app
          JOIN FETCH sa.application appl
          JOIN FETCH sa.programmeOffered po
          JOIN FETCH po.instituteDepartment id
          JOIN FETCH id.department dept
          JOIN FETCH po.programme prog
          JOIN FETCH sa.admissionWindow aw
          WHERE id.institute.instituteId = :instituteId
          ORDER BY sa.id DESC
          """)
    List<SeatAllotment> findAllByInstituteId(@Param("instituteId") Integer instituteId);

    @Query("""
          SELECT COUNT(sa) FROM SeatAllotment sa
          WHERE sa.programmeOffered.instituteDepartment.institute.instituteId = :instituteId
          """)
    Long countByInstituteId(@Param("instituteId") Short instituteId);

    @Query("""
          SELECT COUNT(sa) FROM SeatAllotment sa
          WHERE sa.programmeOffered.instituteDepartment.institute.instituteId = :instituteId
            AND sa.status = :status
          """)
    Long countByInstituteIdAndStatus(@Param("instituteId") Short instituteId, @Param("status") AllotmentStatus status);

    boolean existsByAdmissionWindowAdmissionIdAndRoundTypeAndApplicationApplicationIdAndStatus(Short admissionId,
                                                                                               String roundType, Long applicationId, AllotmentStatus status);

    @Query("""
          SELECT sa FROM SeatAllotment sa
          JOIN FETCH sa.applicant applicant
          JOIN FETCH sa.application application
          JOIN FETCH sa.programmeOffered po
          JOIN FETCH po.programme programme
          WHERE po.instituteDepartment.institute.instituteId = :instituteId
          AND sa.status = :status
          ORDER BY programme.programmeName ASC, applicant.firstName ASC
          """)
    List<SeatAllotment> findByInstituteAndStatusWithDetails(@Param("instituteId") Short instituteId,
                                                            @Param("status") AllotmentStatus status);

    @Query("SELECT COUNT(s) > 0 FROM SeatAllotment s " +
    	       "WHERE s.applicant.applicantId = :applicantId " +
    	       "AND s.admissionWindow.admissionId = :windowId " +
    	       "AND s.status NOT IN :excludedStatuses")
    	boolean hasActiveAllotment(
    	        @Param("applicantId") Long applicantId,
    	        @Param("windowId") Long windowId,
    	        @Param("excludedStatuses") List<AllotmentStatus> excludedStatuses);
    List<SeatAllotment> findByProgrammeOffered_InstituteDepartment_Institute_InstituteIdAndStatusIn(Short instituteId,
                                                                                                    Collection<AllotmentStatus> statuses);

    // -------------------------
    // PAGINATION & RECOVERY (NEW/UPDATED)
    // -------------------------

    @Query("""
          SELECT sa FROM SeatAllotment sa
          JOIN FETCH sa.applicant applicant
          JOIN FETCH sa.application application
          JOIN FETCH sa.programmeOffered po
          JOIN FETCH po.programme programme
          WHERE po.instituteDepartment.institute.instituteId = :instituteId
          AND sa.status IN :statuses
          ORDER BY sa.id DESC
          """)
    Page<SeatAllotment> findByInstituteIdAndStatusInPaged(
            @Param("instituteId") Short instituteId,
            @Param("statuses") Collection<AllotmentStatus> statuses,
            Pageable pageable);

    @Query("""
          SELECT sa FROM SeatAllotment sa
          JOIN FETCH sa.admissionWindow aw
          JOIN FETCH sa.programmeOffered po
          JOIN FETCH po.programme p
          JOIN FETCH po.instituteDepartment id
          JOIN FETCH id.institute i
          WHERE sa.applicant.applicantNo = :applicantNo
          ORDER BY sa.id DESC
          """)
    List<SeatAllotment> findByApplicant_ApplicantNoOrderByIdDesc(@Param("applicantNo") String applicantNo);

    // --- UPDATE THIS EXISTING QUERY ---
    @Query("""
          SELECT sa FROM SeatAllotment sa
          JOIN FETCH sa.applicant applicant
          JOIN FETCH sa.application application
          JOIN FETCH sa.programmeOffered po
          JOIN FETCH po.programme programme
          WHERE po.instituteDepartment.institute.instituteId = :instituteId
          AND sa.status IN :statuses
          AND (:programmeId IS NULL OR programme.programmeId = :programmeId)
          ORDER BY sa.id DESC
          """)
    Page<SeatAllotment> findByInstituteIdAndStatusInPaged(
            @Param("instituteId") Short instituteId,
            @Param("statuses") Collection<AllotmentStatus> statuses,
            @Param("programmeId") Short programmeId, // <-- NEW PARAMETER
            Pageable pageable);

    // --- ADD THIS NEW METHOD FOR THE DROPDOWN ---
    @Query("""
          SELECT DISTINCT po.programme 
          FROM SeatAllotment sa 
          JOIN sa.programmeOffered po 
          WHERE po.instituteDepartment.institute.instituteId = :instituteId
          """)
    List<nic.meg.mcap.entities.Programme> findDistinctProgrammesByInstituteId(@Param("instituteId") Short instituteId);
}