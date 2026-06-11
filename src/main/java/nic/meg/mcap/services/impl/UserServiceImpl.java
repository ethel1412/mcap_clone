package nic.meg.mcap.services.impl;

import java.text.SimpleDateFormat;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import nic.meg.mcap.audit.AuditTable;
import nic.meg.mcap.audit.AuditTableRepository;
import nic.meg.mcap.dto.request.ChangePasswordRequestDTO;
import nic.meg.mcap.dto.request.ForcePasswordChangeRequestDTO;
import nic.meg.mcap.dto.request.UserDTO;
import nic.meg.mcap.dto.request.UserUpdateDTO;
import nic.meg.mcap.dto.response.RoleDTO;
import nic.meg.mcap.dto.response.UserActivitiesResponse;
import nic.meg.mcap.dto.response.UserResponse;
import nic.meg.mcap.dto.response.UserResponseByCode;
import nic.meg.mcap.entities.LoginActivity;
import nic.meg.mcap.entities.Role;
import nic.meg.mcap.entities.User;
import nic.meg.mcap.repositories.LoginActivitieRepository;
import nic.meg.mcap.repositories.RoleRepository;
import nic.meg.mcap.repositories.UserRepository;
import nic.meg.mcap.services.AuthenticationService;
import nic.meg.mcap.services.UserService;
import nic.meg.mcap.utils.RSAUtil;
import nic.meg.mcap.utils.SecurityConstants;

@Service
public class UserServiceImpl implements UserService, UserDetailsService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private LoginActivitieRepository loginActivitiesRepository;

	@Autowired
	private ModelMapper modelMapper;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private AuthenticationService authService;

	@Autowired
	private AuditTableRepository auditTable;

	@Autowired
	private RSAUtil rsaUtil;

	private static final Pattern PWD_PATTERN = Pattern.compile(SecurityConstants.PASSWORD_REGEX);
	private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

	// Get List of Users
	@Override
	public Page<UserResponse> getListUsers(Pageable pageable) {
		Page<User> users = userRepository.findAll(pageable);

		return users.map(user -> {
			Role role = user.getRole();
			RoleDTO roleDTO = null;
			if (role != null) {
				roleDTO = new RoleDTO(role.getRoleId(), role.getRoleName());
			}
			return UserResponse.builder().userCode(user.getUserCode()).username(user.getUsername())
					.accountNonExpired(user.getAccountNonExpired()).accountNonLocked(user.getAccountNonLocked())
					.enabled(user.getEnabled())
					.dateJoined(user.getDateJoined() != null ? user.getDateJoined().toString() : null).role(roleDTO)
					.build();
		});
	}

	@Override
	public Page<UserActivitiesResponse> getListUsersActivities(Pageable pageable) {
		Page<LoginActivity> loginActivitiesPage = loginActivitiesRepository.findAll(pageable);

		SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy, HH:mm");

		return loginActivitiesPage.map(activity -> {
			// THE FIX: Safe null check for the User object
			String displayUsername = (activity.getUser() != null) ? activity.getUser().getUsername()
					: activity.getUsernameAttempt();

			return UserActivitiesResponse.builder().username(displayUsername).isSuccess(activity.getIsSuccess())
					.ipAddress(activity.getIpAddress()).time(format.format(activity.getTime())).build();
		});
	}

	// Update role of user
	@Override
	public User updateUser(UserUpdateDTO userUpdate) {
		Optional<User> userOpt = userRepository.findByUserCode(userUpdate.getUserCode());

		if (userOpt.isEmpty()) {
			throw new RuntimeException("User not found with userCode: " + userUpdate.getUserCode());
		}

		User user = userOpt.get();

		if (userUpdate.getEnabled() != null) {
			user.setEnabled(userUpdate.getEnabled());
		}

		if (userUpdate.getAccountNonExpired() != null) {
			user.setAccountNonExpired(userUpdate.getAccountNonExpired());
		}

		if (userUpdate.getAccountNonLocked() != null) {
			user.setAccountNonLocked(userUpdate.getAccountNonLocked());
		}

		if (userUpdate.getCredentialsNonExpired() != null) {
			user.setCredentialsNonExpired(userUpdate.getCredentialsNonExpired());
		}

		if (userUpdate.getIsSuperuser() != null) {
			user.setIsSuperuser(userUpdate.getIsSuperuser());
		}

		if (userUpdate.getRoleName() != null && !userUpdate.getRoleName().trim().isEmpty()) {
			Optional<Role> roleOpt = roleRepository.findByRoleName(userUpdate.getRoleName());
			if (roleOpt.isEmpty()) {
				throw new RuntimeException("Role not found with name: " + userUpdate.getRoleName());
			}
			user.setRole(roleOpt.get());
		}

		return userRepository.save(user);
	}

	@Override
	public UserResponseByCode getUserByUserCode(UUID userCode) {
		Optional<User> user = userRepository.findByUserCode(userCode);
		return modelMapper.map(user, UserResponseByCode.class);
	}

	@Override
	public UserResponseByCode getUserByUsername() {
		final String currentUserName = SecurityContextHolder.getContext().getAuthentication().getName();
		Optional<User> user = userRepository.findByUsername(currentUserName);
		return modelMapper.map(user, UserResponseByCode.class);
	}

	@Override
	@Transactional
	public boolean changePassword(ChangePasswordRequestDTO passwordChange) {

		final String currentUserName = SecurityContextHolder.getContext().getAuthentication().getName();

		User user = userRepository.findByUsername(currentUserName)
				.orElseThrow(() -> new UsernameNotFoundException("User not found: " + currentUserName));

		Long loginId;
		loginId = authService.getCurrentUserLoginActivityId();
		// ✅ decrypt passwords (handle only crypto-related exception if needed)
		String decryptedOldPassword;
		String decryptedNewPassword;
		String decryptedConfirmPassword;

		try {
			decryptedOldPassword = rsaUtil.decrypt(passwordChange.getCurrentPassword());
			decryptedNewPassword = rsaUtil.decrypt(passwordChange.getNewPassword());
			decryptedConfirmPassword = rsaUtil.decrypt(passwordChange.getConfirmPassword());
		} catch (Exception e) {
			throw new IllegalStateException("Error decrypting password", e);
		}

		// ✅ validations
		if (!decryptedNewPassword.equals(decryptedConfirmPassword)) {
			throw new BadCredentialsException("Passwords do not match");
		}

		if (!PWD_PATTERN.matcher(decryptedNewPassword).matches()) {
			throw new BadCredentialsException("New password does not meet complexity requirements");
		}

		if (!passwordEncoder.matches(decryptedOldPassword, user.getPassword())) {
			throw new BadCredentialsException("Invalid current password");
		}

		// ✅ update password
		user.setPassword(passwordEncoder.encode(decryptedNewPassword));

		// ✅ audit
		auditTable.save(AuditTable.builder().id(loginId).entityId(user.getUserId().toString()).entityName("User")
				.action("update password (general)").build());

		userRepository.save(user);

		return true;
	}

	@Override
	@Transactional
	public User forceChangePassword(ForcePasswordChangeRequestDTO request, User loggedInUser) {

		loggedInUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
		loggedInUser.setPasswordChangeRequired(false); // Mark as no longer required
		User updatedUser = userRepository.save(loggedInUser);

		// Optional: Audit this action
		try {
			Long loginId = authService.getCurrentUserLoginActivityId(); // Get login ID if applicable
			auditTable.save(AuditTable.builder().id(loginId != null ? loginId : -1L) // Provide a default if loginId can
																						// be null
					.entityId(loggedInUser.getUserId().toString()).entityName("User")
					.action("update password (forced first login)").build());
		} catch (Exception e) {
			logger.info("Error auditing forced password change: "); // Or use a proper logger
		}

		return updatedUser;
	}

	@Override
	public UserResponseByCode getUserByUsername(String username) {
		Optional<User> user = userRepository.findByUsernameStartsWithIgnoreCase(username);
		return user.map(u -> modelMapper.map(u, UserResponseByCode.class)).orElse(null);
	}

	@Override
	public User getUser() {
		final String currentUserName = SecurityContextHolder.getContext().getAuthentication().getName();
		Optional<User> user = userRepository.findByUsername(currentUserName);
		return user.orElse(null);
	}

	@Override
	public User createUser(UserDTO userDTO) {
		User user = modelMapper.map(userDTO, User.class);

		user.setUserCode(UUID.randomUUID());
		user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
		user.setIsSuperuser(Boolean.TRUE.equals(userDTO.getIsSuperuser()));
		user.setEnabled(Boolean.TRUE.equals(userDTO.getEnabled()));
		user.setAccountNonExpired(Boolean.TRUE.equals(userDTO.getAccountNonExpired()));
		user.setAccountNonLocked(Boolean.TRUE.equals(userDTO.getAccountNonLocked()));
		user.setCredentialsNonExpired(Boolean.TRUE.equals(userDTO.getCredentialsNonExpired()));

		user.setPasswordChangeRequired(false);

		Role role = roleRepository.findByRoleName(userDTO.getRoleName())
				.orElseThrow(() -> new IllegalArgumentException("Role not found: " + userDTO.getRoleName()));
		user.setRole(role);
		return userRepository.save(user);
	}

	@Override
	@Transactional
	public User lockUnlockUser(UUID userCode, boolean unlock) {
		User user = userRepository.findByUserCode(userCode)
				.orElseThrow(() -> new RuntimeException("User not found: " + userCode));

		user.setAccountNonLocked(unlock);
		User saved = userRepository.saveAndFlush(user);
		return saved;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return userRepository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
	}

}
