package nic.meg.mcap.controllers;

import lombok.RequiredArgsConstructor;
import nic.meg.mcap.dto.request.MenuRequestDTO;
import nic.meg.mcap.dto.request.RoleRequestDTO;
import nic.meg.mcap.dto.response.MenuResponseDTO;
import nic.meg.mcap.dto.response.RoleResponseDTO;
import nic.meg.mcap.services.MenuService;
import nic.meg.mcap.services.RoleService;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
@RequestMapping("/role-data")
@RequiredArgsConstructor
public class RoleDataController {

    private final RoleService roleService;
    private final MenuService menuService;

    // ----- ROLE APIs -----
    @GetMapping("/roles")
    public List<RoleResponseDTO> getAllRoles() {
        return roleService.getAllRoles();
    }

    @PostMapping("/roles")
    public RoleResponseDTO createRole(@RequestBody RoleRequestDTO roleDTO) {
        return roleService.createRole(roleDTO);
    }

    // ----- MENU APIs -----
    @GetMapping("/menus")
    public List<MenuResponseDTO> getAllMenus() {
        return menuService.getAllMenus();
    }

    @PostMapping("/menus")
    public MenuResponseDTO createMenu(@RequestBody MenuRequestDTO menuDTO) {
        return menuService.createMenu(menuDTO);
    }

    // ----- ROLE-MENU ACCESS APIs -----
    @GetMapping("/role-menu-access/{roleId}")
    public List<MenuResponseDTO> getMenusByRole(@PathVariable String roleId) {
        return roleService.getMenusByRole(roleId);
    }

    @PostMapping("/role-menu-access/{roleId}")
    public void updateRoleMenus(@PathVariable String roleId, @RequestBody List<Short> menuIds) {
        roleService.updateRoleMenus(roleId, menuIds);
    }
}
