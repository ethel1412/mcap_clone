package nic.meg.mcap.services;

import java.util.List;
import nic.meg.mcap.dto.request.RoleRequestDTO;
import nic.meg.mcap.dto.response.RoleResponseDTO;
import nic.meg.mcap.dto.response.MenuResponseDTO;

public interface RoleService {
    List<RoleResponseDTO> getAllRoles();
    RoleResponseDTO createRole(RoleRequestDTO roleDTO);
    List<MenuResponseDTO> getMenusByRole(String roleId);
    void updateRoleMenus(String roleId, List<Short> menuIds);
}
