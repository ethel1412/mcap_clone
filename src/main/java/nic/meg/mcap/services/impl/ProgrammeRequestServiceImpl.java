package nic.meg.mcap.services.impl;

import nic.meg.mcap.dto.request.ProgrammeRequestDTO;
import nic.meg.mcap.dto.response.ProgrammeRequestResponseDTO;
import nic.meg.mcap.entities.*;
import nic.meg.mcap.enums.Shift; // <-- Added Import for Shift
import nic.meg.mcap.repositories.*;
import nic.meg.mcap.services.ProgrammeRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProgrammeRequestServiceImpl implements ProgrammeRequestService {

    @Autowired private ProgrammeRequestRepository requestRepo;
    @Autowired private ProgrammeRepository programmeMasterRepo;
    @Autowired private ProgrammesOfferedRepository programmeOfferedRepo;
    @Autowired private InstituteRepository instituteRepo;
    @Autowired private StreamRepository streamRepo;
    @Autowired private InstituteDepartmentRepository deptRepo;

    @Override
    @Transactional
    public void submitRequest(Short instituteId, ProgrammeRequestDTO dto) {

        // 1. Clean the input
        String programmeName = dto.getProgrammeName().trim();

        // 2. VALIDATION: Check Master List
        if (programmeMasterRepo.existsByProgrammeNameIgnoreCase(programmeName)) {
            throw new IllegalArgumentException(
                    "The programme '" + programmeName + "' already exists in the Master List. " +
                            "Please search and add it from the 'Add Programme' menu instead."
            );
        }

        // 3. VALIDATION: Check Pending Requests for this Institute
        boolean requestExists = requestRepo.existsByInstitute_InstituteIdAndProgrammeNameIgnoreCaseAndStatus(
                instituteId,
                programmeName,
                "PENDING"
        );

        if (requestExists) {
            throw new IllegalStateException(
                    "You have already requested '" + programmeName + "' and it is currently PENDING approval."
            );
        }

        // 4. Proceed with Normal Logic
        Institute institute = instituteRepo.findById(instituteId)
                .orElseThrow(() -> new RuntimeException("Institute not found"));

        Stream stream = streamRepo.findById(dto.getStreamId())
                .orElseThrow(() -> new RuntimeException("Stream not found"));

        InstituteDepartment department = deptRepo.findById(dto.getInstituteDepartmentId())
                .orElseThrow(() -> new RuntimeException("Department not found"));

        ProgrammeRequest req = new ProgrammeRequest();
        req.setProgrammeName(programmeName);
        req.setProgrammeLevel(dto.getProgrammeLevel());
        req.setStream(stream);
        req.setInstitute(institute);
        req.setInstituteDepartment(department);
        req.setStatus("PENDING");
        req.setCreatedAt(LocalDateTime.now());

        requestRepo.save(req);
    }

    @Override
    @Transactional
    public void approveRequest(Long requestId) {
        ProgrammeRequest req = requestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        
        if (!"PENDING".equals(req.getStatus())) {
            throw new RuntimeException("Request is already processed");
        }

        Programme masterProgramme = programmeMasterRepo
                .findByProgrammeNameAndProgrammeLevel(req.getProgrammeName(), req.getProgrammeLevel())
                .orElseGet(() -> {
                    Programme newProg = new Programme();
                    newProg.setProgrammeName(req.getProgrammeName());
                    newProg.setProgrammeLevel(req.getProgrammeLevel());
                    newProg.setStream(req.getStream());
                    newProg.setDepartment(req.getInstituteDepartment().getDepartment());
                    return programmeMasterRepo.save(newProg);
                });

        InstituteDepartment dept = req.getInstituteDepartment();

        if (dept == null) {
            throw new RuntimeException("No department associated with this request.");
        }

        // --- UPDATED FIX ---
        // Check using the new method name and default shift (Shift.NA)
        boolean exists = programmeOfferedRepo.existsByProgrammeAndInstituteDepartmentAndShift(masterProgramme, dept, Shift.NA);

        if (!exists) {
            ProgrammeOffered po = new ProgrammeOffered();
            po.setProgramme(masterProgramme);
            po.setInstituteDepartment(dept);
            po.setShift(Shift.NA); // Set the default shift
            programmeOfferedRepo.save(po);
        }

        req.setStatus("APPROVED");
        requestRepo.save(req);
    }

    @Override
    @Transactional
    public void rejectRequest(Long requestId, String reason) {
        ProgrammeRequest req = requestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        req.setStatus("REJECTED");
        req.setRejectionReason(reason);
        requestRepo.save(req);
    }

    @Override
    public List<ProgrammeRequestResponseDTO> getAllPendingRequests() {
        return requestRepo.findByStatus("PENDING").stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProgrammeRequestResponseDTO> getRequestsByInstitute(Short instituteId) {
        return requestRepo.findByInstitute_InstituteId(instituteId).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    private ProgrammeRequestResponseDTO convertToResponseDTO(ProgrammeRequest entity) {
        ProgrammeRequestResponseDTO dto = new ProgrammeRequestResponseDTO();
        dto.setRequestId(entity.getRequestId());
        dto.setProgrammeName(entity.getProgrammeName());
        dto.setProgrammeLevel(entity.getProgrammeLevel());
        dto.setStreamName(entity.getStream().getStreamName());
        dto.setInstituteName(entity.getInstitute().getInstituteName());
        dto.setStatus(entity.getStatus());
        dto.setRejectionReason(entity.getRejectionReason());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }


    public List<ProgrammeRequestResponseDTO> getAllRequests() {
        // Fetch all entities sorted by date
        List<ProgrammeRequest> allRequests = requestRepo.findAllByOrderByCreatedAtDesc();

        return allRequests.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }
}