package nic.meg.mcap.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import nic.meg.mcap.dto.request.MenuRequestDTO;
import nic.meg.mcap.dto.response.MenuResponseDTO;
import nic.meg.mcap.dto.response.RoleResponseDTO;
import nic.meg.mcap.entities.Menu;
import nic.meg.mcap.entities.PageUrl; // Ensure this is correctly imported
import nic.meg.mcap.repositories.MenuRepository;
import nic.meg.mcap.services.MenuService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {
    private static final Logger logger = LoggerFactory.getLogger(MenuServiceImpl.class);

    private final MenuRepository menuRepository;

    // -------------------------
    // Existing: Navigation menu
    // -------------------------
    @Override
    @Transactional(readOnly = true) // Add Transactional for fetching from DB
    public List<MenuResponseDTO> getMenuForRole(String roleName) {

        List<Menu> menus = menuRepository.findMenusByRoleName(roleName);
        // Group children by parent
        Map<Menu, List<Menu>> childrenMap = menus.stream()
                .filter(m -> m.getParentMenu() != null)
                .collect(Collectors.groupingBy(Menu::getParentMenu));

        // Get top-level menus only
        List<Menu> topMenus = menus.stream()
                .filter(m -> m.getParentMenu() == null)
                .sorted(Comparator.comparing(Menu::getOrderIndex))
                .toList();

        return topMenus.stream()
                .map(m -> toTreeDto(m, childrenMap, roleName)) // Pass roleName down
                .collect(Collectors.toList());
    }

    // Modified to accept roleName
    private MenuResponseDTO toTreeDto(Menu menu, Map<Menu, List<Menu>> childrenMap, String roleName) {
        String url = null;

        // --- SPECIAL LOGIC FOR DASHBOARD URL ---
        // Changed from menu.getMenuId() != null && menu.getMenuId().equals(1)
        // to menu.getMenuId() == 1 for primitive short menuId
        if (menu.getMenuId() == 1) { // Menu ID for Dashboard is 1
            if ("ADMIN".equalsIgnoreCase(roleName)) {
                url = "/admin/dashboard";
            } else if ("INSTITUTE".equalsIgnoreCase(roleName) || "INSTITUTE".equalsIgnoreCase(roleName)) {
                url = "/institute-dashboard";
            } else {
                // Fallback for other roles or if role doesn't match expected dashboard patterns
                // Use default URL from DB or a generic one
                Set<PageUrl> pageUrls = menu.getPageUrls(); // Access the Set
                if (pageUrls != null && !pageUrls.isEmpty()) {
                    url = pageUrls.iterator().next().getPageUrl(); // Get the first URL from the set
                } else {
                    url = "#"; // Default fallback if no pageUrl is associated
                }
            }
        } else {
            // For all other menu items, use the URL from the database
            Set<PageUrl> pageUrls = menu.getPageUrls(); // Access the Set
            if (pageUrls != null && !pageUrls.isEmpty()) {
                url = pageUrls.iterator().next().getPageUrl(); // Get the first URL from the set
            } else {
                url = "#"; // Default fallback if no pageUrl is associated
            }
        }


        // Recursively map children
        List<MenuResponseDTO> children = Optional.ofNullable(childrenMap.get(menu))
                .orElse(Collections.emptyList())
                .stream()
                .sorted(Comparator.comparing(Menu::getOrderIndex))
                .map(child -> toTreeDto(child, childrenMap, roleName)) // Pass roleName in recursive call
                .collect(Collectors.toList());

        MenuResponseDTO dto = new MenuResponseDTO(menu.getMenuName(), menu.getIconClass(), url, children);
        dto.setMenuId(menu.getMenuId());
        dto.setOrderIndex(menu.getOrderIndex());
        return dto;
    }

    @Override
    public List<MenuResponseDTO> getAllMenus() {
        return menuRepository.findAll()
                .stream()
                .map(m -> {
                    List<RoleResponseDTO> roleDTOs = m.getRoles() != null
                            ? m.getRoles().stream()
                            .map(r -> new RoleResponseDTO(r.getRoleId(), r.getRoleName()))
                            .toList()
                            : Collections.emptyList();

                    return new MenuResponseDTO(
                            m.getMenuId(),
                            m.getMenuName(),
                            m.getIconClass(),
                            m.getOrderIndex(),
                            roleDTOs // ✅ assignedRoles
                    );
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MenuResponseDTO createMenu(MenuRequestDTO menuDTO) {
        Menu menu = new Menu();
        menu.setMenuName(menuDTO.getMenuName());
        menu.setIconClass(menuDTO.getIconClass());
        menu.setOrderIndex(menuDTO.getOrderIndex());
        menu.setIsActive(menuDTO.isActive());

        if (menuDTO.getParentMenuId() != null) {
            menuRepository.findById(menuDTO.getParentMenuId())
                    .ifPresent(menu::setParentMenu);
        }

        Menu saved = menuRepository.save(menu);
        return new MenuResponseDTO(saved.getMenuId(), saved.getMenuName(),
                saved.getIconClass(), saved.getOrderIndex());
    }
}