package nic.meg.mcap.services;

import java.util.List;

import nic.meg.mcap.dto.request.InstituteDepartmentRequestDTO;
import nic.meg.mcap.dto.response.InstituteDepartmentResponseDTO;

import nic.meg.mcap.dto.request.InstituteDepartmentBatchAssignRequestDTO;
import org.springframework.transaction.annotation.Transactional;

public interface InstituteDepartmentService {

    List<InstituteDepartmentResponseDTO> getAllInstituteDepartments();

    InstituteDepartmentResponseDTO getInstituteDepartmentById(Integer id);

    // SECURITY ADDED: Require loggedInInstituteId
    InstituteDepartmentResponseDTO createInstituteDepartment(InstituteDepartmentRequestDTO requestDTO, Short loggedInInstituteId);

    // SECURITY ADDED: Require loggedInInstituteId
    void deleteInstituteDepartment(Integer id, Short loggedInInstituteId);

    // SECURITY ADDED: Require loggedInInstituteId
    void assignDepartmentsToInstitute(InstituteDepartmentBatchAssignRequestDTO batchRequest, Short loggedInInstituteId);

    List<InstituteDepartmentResponseDTO> getByInstituteId(Short instituteId);

    // SECURITY ADDED: Require loggedInInstituteId
    void assignDepartmentsToInstitute(InstituteDepartmentRequestDTO dto, Short loggedInInstituteId);

    @Transactional
    InstituteDepartmentResponseDTO updateInstituteDepartment(Integer instituteDepartmentId, InstituteDepartmentRequestDTO requestDTO, Short loggedInInstituteId);
}