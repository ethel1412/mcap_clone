package nic.meg.mcap.services.impl;

import nic.meg.mcap.dto.request.DepartmentRequestDTO;
import nic.meg.mcap.dto.response.DepartmentRequestResponseDTO;
import nic.meg.mcap.entities.*;
import nic.meg.mcap.repositories.*;
import nic.meg.mcap.services.DepartmentRequestService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.util.StringUtils;

@Service
@Transactional
public class DepartmentRequestServiceImpl implements DepartmentRequestService {

    @Autowired
    private DepartmentRequestRepository requestRepo;

    @Autowired
    private InstituteRepository instituteRepo;

    @Autowired
    private DepartmentRepository departmentRepo; 

    @Autowired
    private InstituteDepartmentRepository instituteDepartmentRepo;

    @Override
    public void submitRequest(Short instituteId, DepartmentRequestDTO requestDTO) {
        Institute institute = instituteRepo.findById(instituteId)
                .orElseThrow(() -> new RuntimeException("Institute not found"));

        String nameToCheck = requestDTO.getDepartmentName().trim();

        // --- 1. CHECK: Code Uniqueness (If code is provided) ---
        if (StringUtils.hasText(requestDTO.getDepartmentCode())) {
            if (departmentRepo.existsByDepartmentCodeIgnoreCase(requestDTO.getDepartmentCode().trim())) {
                throw new IllegalArgumentException("Department Code '" + requestDTO.getDepartmentCode().toUpperCase() + "' is already in use.");
            }
        }

        // --- 2. CHECK: Exact Global Name Existence (Case Insensitive) ---
        // matches "Biology" with "biology", but NOT "Bio" with "Biology"
        if (departmentRepo.existsByDepartmentNameIgnoreCase(nameToCheck)) {
            throw new IllegalArgumentException(
                "The department '" + nameToCheck + "' already exists in the system. " +
                "Please search for it in the 'Add Department' dropdown."
            );
        }

        // --- 3. CHECK: Pending Requests (Prevent Double Clicking) ---
        // Check if THIS institute has already requested this exact name
        List<DepartmentRequest> myPending = requestRepo.findByInstitute_InstituteIdAndStatus(instituteId, "PENDING");
        
        for (DepartmentRequest req : myPending) {
            if (req.getDepartmentName().trim().equalsIgnoreCase(nameToCheck)) {
                 throw new IllegalArgumentException("You have already requested '" + nameToCheck + "'. Please wait for approval.");
            }
        }

        // --- 4. SAVE ---
        DepartmentRequest request = new DepartmentRequest();
        request.setInstitute(institute);
        request.setDepartmentName(nameToCheck);
        request.setDepartmentCode(requestDTO.getDepartmentCode());
        request.setHodName(requestDTO.getHodName());
        request.setEmail(requestDTO.getEmail());
        request.setPhone(requestDTO.getPhone());
        request.setStatus("PENDING");
        request.setCreatedAt(LocalDateTime.now());
        
        requestRepo.save(request);
    }

    @Override
    public List<DepartmentRequestResponseDTO> getRequestsByInstitute(Short instituteId) {
        return requestRepo.findByInstitute_InstituteIdOrderByCreatedAtDesc(instituteId)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public List<DepartmentRequestResponseDTO> getAllPendingRequests() {
        return requestRepo.findByStatusOrderByCreatedAtDesc("PENDING")
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public List<DepartmentRequestResponseDTO> getAllRequests() {
        return requestRepo.findAllByOrderByCreatedAtDesc()
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public void approveRequest(Long requestId) {
        DepartmentRequest request = requestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (!"PENDING".equals(request.getStatus())) {
            throw new RuntimeException("Request is already processed.");
        }

        // 1. Handle Master Department Creation/Lookup
        // Note: Ensure DepartmentRepository has 'findByDepartmentNameIgnoreCase(String name)'
        Department masterDept = departmentRepo.findByDepartmentNameIgnoreCase(request.getDepartmentName())
                .orElseGet(() -> {
                    Department newDept = new Department();
                    newDept.setDepartmentName(request.getDepartmentName());
                    newDept.setDepartmentCode(request.getDepartmentCode());
                    // Removed .setActive(true) as it is not in your Department Entity
                    return departmentRepo.save(newDept);
                });

        // 2. Check if Institute already has this department linked
        // FIXED: Using the ID-based method from your Repository
        boolean alreadyLinked = instituteDepartmentRepo.existsByInstituteInstituteIdAndDepartmentDepartmentId(
                request.getInstitute().getInstituteId(), 
                masterDept.getDepartmentId()
        );
        
        if (!alreadyLinked) {
            // 3. Create InstituteDepartment Link
            InstituteDepartment instDept = new InstituteDepartment();
            instDept.setInstitute(request.getInstitute());
            instDept.setDepartment(masterDept);
            instDept.setHodName(request.getHodName());
            instDept.setEmail(request.getEmail());
            instDept.setPhone(request.getPhone());
            instDept.setActive(true); 
            
            instituteDepartmentRepo.save(instDept);
        }

        // 4. Update Request Status
        request.setStatus("APPROVED");
        requestRepo.save(request);
    }

    @Override
    public void rejectRequest(Long requestId, String reason) {
        DepartmentRequest request = requestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (!"PENDING".equals(request.getStatus())) {
            throw new RuntimeException("Request is already processed.");
        }

        request.setStatus("REJECTED");
        request.setRejectionReason(reason);
        requestRepo.save(request);
    }

    // Helper Mapper
    private DepartmentRequestResponseDTO mapToDTO(DepartmentRequest req) {
        DepartmentRequestResponseDTO dto = new DepartmentRequestResponseDTO();
        dto.setRequestId(req.getRequestId());
        dto.setDepartmentName(req.getDepartmentName());
        dto.setDepartmentCode(req.getDepartmentCode());
        dto.setHodName(req.getHodName());
        dto.setEmail(req.getEmail());
        dto.setPhone(req.getPhone());
        dto.setStatus(req.getStatus());
        dto.setRejectionReason(req.getRejectionReason());
        dto.setCreatedAt(req.getCreatedAt());
        dto.setUpdatedAt(req.getUpdatedAt());
        
        if (req.getInstitute() != null) {
            dto.setInstituteId(req.getInstitute().getInstituteId());
            dto.setInstituteName(req.getInstitute().getInstituteName());
        }
        return dto;
    }
}