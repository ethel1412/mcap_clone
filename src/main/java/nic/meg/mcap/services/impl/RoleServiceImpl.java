package nic.meg.mcap.services.impl;

import lombok.RequiredArgsConstructor;
import nic.meg.mcap.dto.request.RoleRequestDTO;
import nic.meg.mcap.dto.response.RoleResponseDTO;
import nic.meg.mcap.dto.response.MenuResponseDTO;
import nic.meg.mcap.entities.Menu;
import nic.meg.mcap.entities.Role;
import nic.meg.mcap.repositories.MenuRepository;
import nic.meg.mcap.repositories.RoleRepository;
import nic.meg.mcap.services.RoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final MenuRepository menuRepository;

    @Override
    public List<RoleResponseDTO> getAllRoles() {
        return roleRepository.findAll()
                .stream()
                .map(role -> new RoleResponseDTO(role.getRoleId(), role.getRoleName()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RoleResponseDTO createRole(RoleRequestDTO roleDTO) {
        if (roleRepository.existsByRoleName(roleDTO.getRoleName())) {
            throw new IllegalArgumentException("Role name already exists: " + roleDTO.getRoleName());
        }
        Role role = new Role();
        role.setRoleId(roleDTO.getRoleId());
        role.setRoleName(roleDTO.getRoleName());

        Role saved = roleRepository.save(role);
        return new RoleResponseDTO(saved.getRoleId(), saved.getRoleName());
    }

    @Override
    public List<MenuResponseDTO> getMenusByRole(String roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));

        return role.getMenus()
                .stream()
                .map(m -> new MenuResponseDTO(
                        m.getMenuId(),
                        m.getMenuName(),
                        m.getIconClass(),
                        m.getOrderIndex()
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateRoleMenus(String roleId, List<Short> menuIds) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));

        Set<Menu> menuSet = new HashSet<>(menuRepository.findAllById(menuIds));
        role.setMenus(menuSet);

        roleRepository.save(role);
    }
}
