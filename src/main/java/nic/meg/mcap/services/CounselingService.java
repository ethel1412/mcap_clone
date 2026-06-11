package nic.meg.mcap.services;

import nic.meg.mcap.dto.request.SubjectPreferenceRequestDTO;
import nic.meg.mcap.dto.request.VerificationRequestDTO;
import nic.meg.mcap.dto.response.*;
import nic.meg.mcap.enums.AllotmentStatus;

import java.util.List;

public interface CounselingService {
    List<CounselingRoundResponseDTO> getApplicantAllotmentOverviews(String applicantNo);

    SeatAllotmentResponseDTO getSeatAllotmentForWindow(String applicantNo, Short admissionWindowId);

    void acceptAllotment(String applicantNo, Long allotmentId);

    void rejectAllotment(String applicantNo, Long allotmentId);

    /**
     * Slide Up: applicant pays and holds this seat but stays eligible for
     * higher-preference seats in subsequent rounds.
     * If no further action is taken before the deadline, the seat auto-reverts to REJECTED.
     */
    void slideUpAllotment(String applicantNo, Long allotmentId);

    SeatAllotmentResponseDTO getSeatAllotmentDetailsById(String applicantNo, Long allotmentId);

    List<SeatAllotmentResponseDTO> getAllotmentsForApplicant(String applicantNo);

    void saveCombinationPreferences(String applicantNo, SubjectPreferenceRequestDTO requestDTO);

    SubjectPreferenceResponseDTO getSavedPreferences(String applicantNo, Long allotmentId);

    List<InstituteAllotmentDTO> getPendingVerificationAllotmentsForInstitute(Short instituteId);

    void performVerification(Long allotmentId, VerificationRequestDTO request, Short instituteId);

    List<InstituteAllotmentDTO> getAllotmentsByStatusList(Short instituteId, List<AllotmentStatus> statuses);

    List<InstituteAllotmentDTO> getApplicantsForProgrammeAndStatus(Integer programmeOfferedId, AllotmentStatus status);

    PagedResponse<InstituteAllotmentDTO> getPagedAllotmentsByStatus(
            Short instituteId,
            List<AllotmentStatus> statuses,
            int page,
            int size
    );
    /**
     * Finds the most recent allotment for an applicant across all windows.
     * Used to recover the state if the user refreshes or logs back in.
     */
    SeatAllotmentResponseDTO getLatestSeatAllotment(String applicantNo);

    PagedResponse<InstituteAllotmentDTO> getPagedAllotmentsByStatus(
            Short instituteId,
            List<AllotmentStatus> statuses,
            Short programmeId, // <-- NEW PARAMETER
            int page,
            int size
    );
}