package nic.meg.mcap.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.validation.Valid;
import nic.meg.mcap.dto.request.ChangePasswordRequestDTO;
import nic.meg.mcap.services.UserService;

@Controller
@RequestMapping("/profile")
@PreAuthorize("isAuthenticated()")

public class ProfileController {
	@Autowired
	private UserService userService;

	@GetMapping("/change-password")
	public String changePassword(Model model) {
		model.addAttribute("activePage", "Change Password");
		return "profile/change-password";
	}

	@PostMapping("/change-password")

	public ResponseEntity<String> changePassword(@Valid @RequestBody ChangePasswordRequestDTO passwordChange) {

		if (userService.changePassword(passwordChange))
			return ResponseEntity.status(HttpStatus.OK).body("Updated Succesfully!");
		else
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to update");
	}
}