package nic.meg.mcap.services;

import nic.meg.mcap.dto.response.AllottedCandidateRowDTO;
import nic.meg.mcap.dto.response.ProgrammeAllocationSummaryDTO;
import nic.meg.mcap.dto.response.SeatAllocationSummaryDTO;
import nic.meg.mcap.dto.response.StudentAllotmentResponseDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface SeatAllotmentService {

    // -------------------------
    // Rounds + phases (NEW)
    // -------------------------

    @Transactional
    SeatAllocationSummaryDTO runAllocationForWindow(String admissionCode, String frontendRoundType, Integer phaseNo);

    List<AllottedCandidateRowDTO> getAllottedCandidates(String admissionCode, String roundType, Integer phaseNo, Integer programmeOfferedId);

    @Transactional(readOnly = true)
    SeatAllocationSummaryDTO getAllocationSummary(String admissionCode, String roundType, Integer phaseNo);

    List<StudentAllotmentResponseDTO> getStudentAllotmentsByInstitute(Integer instituteId);

    Long countAllotmentsByInstitute(Short instituteId);

    Long countAcceptedAllotmentsByInstitute(Short instituteId);

    List<ProgrammeAllocationSummaryDTO> getInstituteProgrammeSummary(Short instituteId, String shiftStr);

    @Transactional(readOnly = true)
    List<ProgrammeAllocationSummaryDTO> getProgrammeAllocationSummary(String admissionCode, Short programmeId, String roundType, Integer phaseNo);

    int countAllotments(String admissionCode, String roundType, Integer phaseNo, Integer programmeOfferedId);
}