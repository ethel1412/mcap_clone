
package nic.meg.mcap.controllers;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import nic.meg.mcap.dto.request.ChangePasswordRequestDTO;
import nic.meg.mcap.dto.request.UserDTO;
import nic.meg.mcap.dto.request.UserUpdateDTO;
import nic.meg.mcap.dto.response.PagedResponse;
import nic.meg.mcap.dto.response.UserActivitiesResponse;
import nic.meg.mcap.dto.response.UserResponse;
import nic.meg.mcap.dto.response.UserResponseByCode;
import nic.meg.mcap.entities.User;
import nic.meg.mcap.services.UserService;

@RestController
@PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
@RequestMapping("/user-management/data")

public class UserDataController {

	@Autowired
	private UserService userService;
	
	@JsonIgnoreProperties(ignoreUnknown = false)
	public record UserCodeRequest(

	        @NotBlank(message = "User code is required")

	        @Pattern(
	                regexp =
	                "^[0-9a-fA-F\\-]{36}$",
	                message = "Invalid UUID format"
	        )
	        String user_code

	) {
	}

	@PostMapping("/create-user")
	@ResponseBody
	public ResponseEntity<?> createUser(@Valid @RequestBody UserDTO userDTO) {

		Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
		Set<ConstraintViolation<UserDTO>> violations = validator.validate(userDTO);

		if (!violations.isEmpty()) {
			List<String> errors = violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.toList());
			return ResponseEntity.badRequest().body(Map.of("errors", errors));
		}

		User created = userService.createUser(userDTO);
		if (created == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("errors", List.of("User could not be created.")));
		}

		return ResponseEntity.ok(Map.of("message", "User created successfully."));
	}

	@GetMapping("/get-users")
	public ResponseEntity<PagedResponse<UserResponse>> getListUsers(
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size) {

		Page<UserResponse> usersPage = userService.getListUsers(PageRequest.of(page, size));

		PagedResponse<UserResponse> response = new PagedResponse<>(usersPage.getContent(), // goes to 'data'
				usersPage.getNumber(), usersPage.getSize(), usersPage.getTotalElements(), usersPage.getTotalPages(),
				usersPage.isLast());

		return ResponseEntity.ok(response);
	}

	@GetMapping("/get-login-activities")
	public ResponseEntity<PagedResponse<UserActivitiesResponse>> getListUserActivities(
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size) {

		Page<UserActivitiesResponse> userActivities = userService.getListUsersActivities(PageRequest.of(page, size));

		PagedResponse<UserActivitiesResponse> response = new PagedResponse<>(userActivities.getContent(),
				userActivities.getNumber(), userActivities.getSize(), userActivities.getTotalElements(),
				userActivities.getTotalPages(), userActivities.isLast());

		return ResponseEntity.ok(response);
	}

	// Get User (All details) By User Code
	@PostMapping("/get-user-by-usercode")
	public ResponseEntity<UserResponseByCode> getUserByUserCode(
	        @Valid @RequestBody UserCodeRequest request)
	        throws IOException {

	    UserResponseByCode user =
	            userService.getUserByUserCode(
	                    UUID.fromString(request.user_code()));

	    return ResponseEntity.ok(user);
	}

	// Get User By Username
	@PostMapping("/get-user-by-username")

	public @ResponseBody ResponseEntity<String> getUserByUsername() throws IOException {
		UserResponseByCode user = userService.getUserByUsername();
		return ResponseEntity.status(HttpStatus.OK).body("/*" + new ObjectMapper().writeValueAsString(user) + "*/");
	}

	// Update Role
	@PostMapping("/update-user")

	public ResponseEntity<String> UpdateUser(@RequestBody UserUpdateDTO userUpdate) {

		if (userService.updateUser(userUpdate) != null)
			return ResponseEntity.status(HttpStatus.OK).body("Updated Succesfully!");
		else
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to update");
	}

	@PostMapping("/change-password")
	public ResponseEntity<String> changePassword(@Valid @RequestBody ChangePasswordRequestDTO passwordChange) {

		boolean updated = userService.changePassword(passwordChange);

		if (updated) {
			return ResponseEntity.ok("Password updated successfully");
		} else {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid current password or request");
		}
	}

	// lock-unlock users
	@PostMapping("/lock-unlock-user")
	public ResponseEntity<String> lockUnlockUser(@RequestBody Map<String, Object> req) {
		UUID userCode = UUID.fromString((String) req.get("userCode"));
		boolean unlock = (Boolean) req.get("lock"); // match key name from frontend
		userService.lockUnlockUser(userCode, unlock);
		return ResponseEntity.ok("User has been " + (unlock ? "unlocked" : "locked") + " successfully.");
	}

}