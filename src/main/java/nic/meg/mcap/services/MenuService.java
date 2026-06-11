package nic.meg.mcap.services;

import java.util.List;
import nic.meg.mcap.dto.request.MenuRequestDTO;
import nic.meg.mcap.dto.response.MenuResponseDTO;

public interface MenuService {
    List<MenuResponseDTO> getMenuForRole(String roleName);
    List<MenuResponseDTO> getAllMenus();
    MenuResponseDTO createMenu(MenuRequestDTO menuDTO);
}
