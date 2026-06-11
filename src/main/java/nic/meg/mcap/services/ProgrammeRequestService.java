package nic.meg.mcap.services;

import nic.meg.mcap.dto.request.ProgrammeRequestDTO;
import nic.meg.mcap.dto.response.ProgrammeRequestResponseDTO;
import java.util.List;

public interface ProgrammeRequestService {
    
    // Scenario B: Institute submits a request
    void submitRequest(Short instituteId, ProgrammeRequestDTO requestDTO);

    // Admin Action: Approve
    void approveRequest(Long requestId);

    // Admin Action: Reject
    void rejectRequest(Long requestId, String reason);

    // View: Admin sees all pending
    List<ProgrammeRequestResponseDTO> getAllPendingRequests();

    // View: Institute sees their own requests (status check)
    List<ProgrammeRequestResponseDTO> getRequestsByInstitute(Short instituteId);

    List<ProgrammeRequestResponseDTO> getAllRequests();
}