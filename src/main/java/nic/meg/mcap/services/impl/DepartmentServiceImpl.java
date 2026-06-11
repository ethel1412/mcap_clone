package nic.meg.mcap.services.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import nic.meg.mcap.dto.response.DepartmentResponseDTO;
import nic.meg.mcap.entities.Department;
import nic.meg.mcap.repositories.DepartmentRepository;
import nic.meg.mcap.services.DepartmentService;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Override
    public List<DepartmentResponseDTO> getAllDepartments() {
        List<Department> departments = departmentRepository.findAll();
        return departments.stream()
                          .map(this::convertToDTO)
                          .collect(Collectors.toList());
    }

    private DepartmentResponseDTO convertToDTO(Department department) {
        DepartmentResponseDTO dto = new DepartmentResponseDTO();
        dto.setDepartmentId(department.getDepartmentId());
        dto.setDepartmentName(department.getDepartmentName());
        dto.setDepartmentCode(department.getDepartmentCode());
        return dto;
    }
}
