package nic.meg.mcap.services;

import nic.meg.mcap.dto.request.DepartmentRequestDTO;
import nic.meg.mcap.dto.response.DepartmentRequestResponseDTO;
import java.util.List;

public interface DepartmentRequestService {
    
    // Institute actions
    void submitRequest(Short instituteId, DepartmentRequestDTO requestDTO);
    List<DepartmentRequestResponseDTO> getRequestsByInstitute(Short instituteId);

    // Controller actions
    List<DepartmentRequestResponseDTO> getAllPendingRequests();
    List<DepartmentRequestResponseDTO> getAllRequests();
    
    void approveRequest(Long requestId);
    void rejectRequest(Long requestId, String reason);
}