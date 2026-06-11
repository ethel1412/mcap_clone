package nic.meg.mcap.config;

import java.security.Principal;
import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import nic.meg.mcap.dto.response.MenuResponseDTO;
import nic.meg.mcap.entities.User;
import nic.meg.mcap.repositories.UserRepository;
import nic.meg.mcap.services.MenuService;

@ControllerAdvice
@Component
public class GlobalMenuAdvice {

    private final MenuService menuService;
    private final UserRepository userRepository;

    public GlobalMenuAdvice(MenuService menuService, UserRepository userRepository) {
        this.menuService = menuService;
        this.userRepository = userRepository;
    }

    @ModelAttribute("menuItems")
    public List<MenuResponseDTO> addMenuItems(Principal principal) {
        if (principal == null) return List.of();

        User currentUser = userRepository.findByUsername(principal.getName()).orElse(null);
        if (currentUser == null) return List.of();

        String roleName = currentUser.getRole().getRoleName();
        return menuService.getMenuForRole(roleName);
    }
}
