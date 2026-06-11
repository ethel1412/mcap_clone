package nic.meg.mcap.services;

import nic.meg.mcap.dto.request.SeatMatrixRequestDTO;
import nic.meg.mcap.dto.response.SeatApprovalRowDTO;
import nic.meg.mcap.dto.response.SeatMatrixResponseDTO;
import nic.meg.mcap.entities.SeatMatrix;
import java.util.List;
import java.util.Optional;

public interface SeatMatrixService {

    // SECURITY ADDED: Requires loggedInInstituteId
    SeatMatrixResponseDTO createOrUpdateSeatMatrix(SeatMatrixRequestDTO request, Short loggedInInstituteId);

    Optional<SeatMatrix> getSeatMatrixByProgrammeOfferedId(Integer programmeOfferedId);

    void deleteSeatMatrix(Long seatMatrixId);

    List<SeatMatrixResponseDTO> getByInstitute(Short instituteId);

    int findSeatsByProgrammeOfferedId(Integer programmeOfferedId);

    List<SeatMatrixResponseDTO> getByAdmissionWindow(Short admissionWindowId);

    // SECURITY ADDED: Requires loggedInInstituteId
    void sendForApproval(Short admissionWindowId, List<Integer> programmeOfferedIds, Short loggedInInstituteId);

    List<SeatApprovalRowDTO> getPendingApprovals();
    List<SeatApprovalRowDTO> getAllSeatApprovals();
    void approveSeatMatrix(Long id);
    void rejectSeatMatrix(Long id, String reason);

}