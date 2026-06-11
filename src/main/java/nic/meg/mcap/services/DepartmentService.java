package nic.meg.mcap.services;

import java.util.List;
import nic.meg.mcap.dto.response.DepartmentResponseDTO;

public interface DepartmentService {
    List<DepartmentResponseDTO> getAllDepartments();
}
