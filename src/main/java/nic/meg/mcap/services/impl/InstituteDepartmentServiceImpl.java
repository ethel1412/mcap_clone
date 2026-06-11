package nic.meg.mcap.services.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import nic.meg.mcap.dto.request.InstituteDepartmentBatchAssignRequestDTO;
import nic.meg.mcap.dto.request.InstituteDepartmentRequestDTO;
import nic.meg.mcap.dto.response.InstituteDepartmentResponseDTO;
import nic.meg.mcap.entities.Department;
import nic.meg.mcap.entities.Institute;
import nic.meg.mcap.entities.InstituteDepartment;
import nic.meg.mcap.repositories.DepartmentRepository;
import nic.meg.mcap.repositories.InstituteDepartmentRepository;
import nic.meg.mcap.repositories.InstituteRepository;
import nic.meg.mcap.services.InstituteDepartmentService;

@Service
public class InstituteDepartmentServiceImpl implements InstituteDepartmentService {

    @Autowired
    private InstituteDepartmentRepository instituteDepartmentRepository;

    @Autowired
    private InstituteRepository instituteRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Override
    public List<InstituteDepartmentResponseDTO> getAllInstituteDepartments() {
        List<InstituteDepartment> mappings = instituteDepartmentRepository.findAll();
        List<InstituteDepartmentResponseDTO> dtos = new ArrayList<>();
        for (InstituteDepartment mapping : mappings) {
            dtos.add(convertToDTO(mapping));
        }
        return dtos;
    }

    @Override
    public InstituteDepartmentResponseDTO getInstituteDepartmentById(Integer id) {
        InstituteDepartment mapping = instituteDepartmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("InstituteDepartment not found with ID: " + id));
        return convertToDTO(mapping);
    }

    @Override
    @Transactional
    public InstituteDepartmentResponseDTO createInstituteDepartment(InstituteDepartmentRequestDTO requestDTO, Short loggedInInstituteId) {
        // 🚨 SECURITY AUDIT LOCK 🚨
        if (!requestDTO.getInstituteId().equals(loggedInInstituteId)) {
            throw new SecurityException("Unauthorized: You cannot assign a department to another institute.");
        }

        Institute institute = instituteRepository.findById(requestDTO.getInstituteId())
                .orElseThrow(() -> new EntityNotFoundException("Institute not found with ID: " + requestDTO.getInstituteId()));

        Department department = departmentRepository.findById(requestDTO.getDepartmentId())
                .orElseThrow(() -> new EntityNotFoundException("Department not found with ID: " + requestDTO.getDepartmentId()));

        boolean exists = instituteDepartmentRepository.existsByInstituteInstituteIdAndDepartmentDepartmentId(
                institute.getInstituteId(), department.getDepartmentId());
        if (exists) {
            throw new IllegalStateException("InstituteDepartment mapping already exists for instituteId: "
                    + institute.getInstituteId() + " and departmentId: " + department.getDepartmentId());
        }

        InstituteDepartment mapping = new InstituteDepartment();
        mapping.setInstitute(institute);
        mapping.setDepartment(department);
        mapping.setActive(requestDTO.isActive());
        mapping.setHodName(requestDTO.getHodName());
        mapping.setEmail(requestDTO.getEmail());
        mapping.setPhone(requestDTO.getPhone());

        InstituteDepartment saved = instituteDepartmentRepository.save(mapping);
        return convertToDTO(saved);
    }

    @Override
    @Transactional
    public void deleteInstituteDepartment(Integer id, Short loggedInInstituteId) {
        InstituteDepartment mapping = instituteDepartmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("InstituteDepartment not found with ID: " + id));

        // 🚨 SECURITY AUDIT LOCK 🚨
        if (!mapping.getInstitute().getInstituteId().equals(loggedInInstituteId)) {
            throw new SecurityException("Unauthorized: You do not have permission to remove this department.");
        }

        instituteDepartmentRepository.delete(mapping);
    }

    @Override
    @Transactional
    public void assignDepartmentsToInstitute(InstituteDepartmentBatchAssignRequestDTO batchRequest, Short loggedInInstituteId) {
        // 🚨 SECURITY AUDIT LOCK 🚨
        if (!batchRequest.getInstituteId().equals(loggedInInstituteId)) {
            throw new SecurityException("Unauthorized: You cannot assign departments to another institute.");
        }

        Institute institute = instituteRepository.findById(batchRequest.getInstituteId())
                .orElseThrow(() -> new EntityNotFoundException("Institute not found with ID: " + batchRequest.getInstituteId()));

        List<InstituteDepartment> newMappings = new ArrayList<>();

        for (Short deptId : batchRequest.getDepartmentIds()) {
            Department department = departmentRepository.findById(deptId)
                    .orElseThrow(() -> new EntityNotFoundException("Department not found with ID: " + deptId));

            boolean exists = instituteDepartmentRepository.existsByInstituteInstituteIdAndDepartmentDepartmentId(
                    institute.getInstituteId(), department.getDepartmentId());

            if (!exists) {
                InstituteDepartment mapping = new InstituteDepartment();
                mapping.setInstitute(institute);
                mapping.setDepartment(department);
                mapping.setActive(batchRequest.isActive());
                mapping.setHodName(batchRequest.getHodName());
                mapping.setEmail(batchRequest.getEmail());
                mapping.setPhone(batchRequest.getPhone());
                newMappings.add(mapping);
            }
        }

        instituteDepartmentRepository.saveAll(newMappings);
    }

    private InstituteDepartmentResponseDTO convertToDTO(InstituteDepartment mapping) {
        InstituteDepartmentResponseDTO dto = new InstituteDepartmentResponseDTO();
        dto.setInstituteDepartmentId(mapping.getInstituteDepartmentId());
        dto.setInstituteId(mapping.getInstitute().getInstituteId());
        dto.setInstituteName(mapping.getInstitute().getInstituteName());
        dto.setDepartmentId(mapping.getDepartment().getDepartmentId());
        dto.setDepartmentName(mapping.getDepartment().getDepartmentName());

        // --- ADDED THIS LINE TO MAP THE CODE ---
        dto.setDepartmentCode(mapping.getDepartment().getDepartmentCode());

        dto.setActive(mapping.isActive());
        dto.setHodName(mapping.getHodName());
        dto.setEmail(mapping.getEmail());
        dto.setPhone(mapping.getPhone());
        return dto;
    }

    @Override
    public List<InstituteDepartmentResponseDTO> getByInstituteId(Short instituteId) {
        List<InstituteDepartment> mappings =
                instituteDepartmentRepository.findByInstituteInstituteIdOrderByDepartmentDepartmentNameAsc(instituteId);

        return mappings.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void assignDepartmentsToInstitute(InstituteDepartmentRequestDTO dto, Short loggedInInstituteId) {
        InstituteDepartmentBatchAssignRequestDTO batchDTO = new InstituteDepartmentBatchAssignRequestDTO();
        batchDTO.setInstituteId(dto.getInstituteId());
        batchDTO.setDepartmentIds(Collections.singletonList(dto.getDepartmentId()));
        batchDTO.setHodName(dto.getHodName());
        batchDTO.setEmail(dto.getEmail());
        batchDTO.setPhone(dto.getPhone());

        assignDepartmentsToInstitute(batchDTO, loggedInInstituteId);
    }

    @Transactional
    @Override
    public InstituteDepartmentResponseDTO updateInstituteDepartment(Integer instituteDepartmentId, InstituteDepartmentRequestDTO requestDTO, Short loggedInInstituteId) {
        InstituteDepartment instituteDepartment = instituteDepartmentRepository.findById(instituteDepartmentId)
                .orElseThrow(() -> new EntityNotFoundException("Institute Department not found with ID: " + instituteDepartmentId));

        // 🚨 SECURITY AUDIT LOCK 🚨
        if (!instituteDepartment.getInstitute().getInstituteId().equals(loggedInInstituteId)) {
            throw new SecurityException("Unauthorized: You do not have permission to update this department.");
        }

        instituteDepartment.setHodName(requestDTO.getHodName());
        instituteDepartment.setEmail(requestDTO.getEmail());
        instituteDepartment.setPhone(requestDTO.getPhone());
        instituteDepartment.setActive(requestDTO.isActive());

        InstituteDepartment saved = instituteDepartmentRepository.save(instituteDepartment);

        return convertToDTO(saved);
    }

}