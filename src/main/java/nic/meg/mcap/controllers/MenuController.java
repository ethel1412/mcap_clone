package nic.meg.mcap.controllers;

import java.security.Principal;
import java.util.List;

// Import for logging
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import nic.meg.mcap.dto.response.MenuResponseDTO;
import nic.meg.mcap.entities.User;
import nic.meg.mcap.repositories.UserRepository;
import nic.meg.mcap.services.MenuService;


@RestController
public class MenuController {

	// Logger instance
	private static final Logger logger = LoggerFactory.getLogger(MenuController.class);

	private final MenuService menuService;
	private final UserRepository userRepository;

	public MenuController(MenuService menuService, UserRepository userRepository) {
		this.menuService = menuService;
		this.userRepository = userRepository;
	}

	/**
	 * Returns menu items for the currently logged-in user as JSON.
	 */
	@PreAuthorize("isAuthenticated()")
	@GetMapping(
		    value = "/menu",
		    produces = MediaType.APPLICATION_JSON_VALUE
		)
	public List<MenuResponseDTO> getMenuForCurrentUser(Principal principal) {
		if (principal == null) {
			return List.of(); // No logged-in user
		}

		User user = userRepository.findByUsername(principal.getName()).orElse(null);

		if (user == null) {
			return List.of();
		}

		if (user.getRole() == null) {
			return List.of();
		}

		String roleName = user.getRole().getRoleName();
		List<MenuResponseDTO> menu = menuService.getMenuForRole(roleName);
		return menu;
	}
}