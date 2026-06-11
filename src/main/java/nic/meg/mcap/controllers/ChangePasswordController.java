package nic.meg.mcap.controllers;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import nic.meg.mcap.dto.request.ForcePasswordChangeRequestDTO; // Use the new DTO
import nic.meg.mcap.dto.request.ResetPasswordDTO;
import nic.meg.mcap.entities.Applicant;
import nic.meg.mcap.entities.User;
import nic.meg.mcap.repositories.ApplicantRepository;
import nic.meg.mcap.repositories.UserRepository;
import nic.meg.mcap.utils.RSAUtil;
import nic.meg.mcap.utils.SecurityConstants;

@Controller
public class ChangePasswordController {

	private static final Logger logger = LoggerFactory.getLogger(ChangePasswordController.class);

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private RSAUtil rsaUtil;

	@Autowired
	private ApplicantRepository applicantRepository;

	@GetMapping("/change-password-form")
	public String showChangePasswordForm(Model model, Principal principal) {
		if (principal == null) {
			return "redirect:/login"; // Redirect if not logged in
		}
		// Get the username of the currently logged-in user
		String username = principal.getName();
		model.addAttribute("username", username); // Pass username to the HTML
		model.addAttribute("forcePasswordChangeRequestDTO", new ForcePasswordChangeRequestDTO());
		return "change-password-form";
	}

	@PostMapping("/change-password-form")
	public String forceChangePassword(
			@ModelAttribute("forcePasswordChangeRequestDTO") @Valid ForcePasswordChangeRequestDTO forcePasswordChangeRequestDTO,
			BindingResult result, Principal principal, RedirectAttributes redirectAttributes, Model model) {

		if (principal == null) {
			return "redirect:/login";
		}

		String username = principal.getName();
		Optional<User> userOpt = userRepository.findByUsername(username);

		if (userOpt.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "User not found.");
			return "redirect:/login";
		}

		User user = userOpt.get();

		if (result.hasErrors()) {
			model.addAttribute("username", username);
			model.addAttribute("forcePasswordChangeRequestDTO", forcePasswordChangeRequestDTO); // Keep entered values
			return "change-password-form";
		}

		String decryptedNewPassword;
		String decryptedConfirmPassword;

		try {
			decryptedNewPassword = rsaUtil.decrypt(forcePasswordChangeRequestDTO.getNewPassword());
			decryptedConfirmPassword = rsaUtil.decrypt(forcePasswordChangeRequestDTO.getConfirmPassword());
		} catch (javax.crypto.BadPaddingException | javax.crypto.IllegalBlockSizeException e) {
			result.reject("password.invalid", "Invalid encrypted password data.");
			return "change-password-form";
		} catch (java.security.GeneralSecurityException e) {
			result.reject("password.decrypt.error", "Encryption error. Please try again.");
			return "change-password-form";
		}
		if (!decryptedNewPassword.equals(decryptedConfirmPassword)) {
			model.addAttribute("username", username);
			model.addAttribute("passwordMismatchError", "New password and confirmation do not match.");
			model.addAttribute("forcePasswordChangeRequestDTO", forcePasswordChangeRequestDTO);
			return "change-password-form";
		}

		if (!decryptedNewPassword.matches(SecurityConstants.PASSWORD_REGEX)) {
			model.addAttribute("username", username);
			model.addAttribute("passwordError",
					"Password must include at least one uppercase letter, one lowercase letter, one digit, one special character, and contain no spaces and should be minumm 8 characters");
			return "change-password-form";
		}

		user.setPassword(passwordEncoder.encode(decryptedNewPassword));
		user.setPasswordChangeRequired(false);
		user.setTempPlaintextPassword(null);
		userRepository.save(user);

		// Update the security context with the new password
		Authentication newAuthentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(newAuthentication);

		redirectAttributes.addFlashAttribute("successMessage", "Your password has been changed successfully.");
		// Redirect to the appropriate dashboard
		if (user.getRole().getRoleName().equals("INSTITUTE")) {
			return "redirect:/institute-dashboard"; // Institute dashboard
		} else if (user.getRole().getRoleName().equals("ADMIN")) {
			return "redirect:/admin/dashboard"; // Admin dashboard
		} else if (user.getRole().getRoleName().equals("APPLICANT")) {
			return "redirect:/applicant/dashboard"; // Applicant dashboard
		} else {
			return "redirect:/"; // Default
		}
	}

	// Change password

	@GetMapping("/reset-password")
	public String showResetPasswordPage(Model model) {

		model.addAttribute("resetPasswordDTO", new ResetPasswordDTO());

		return "applicant/reset-password";
	}

	@PostMapping("/reset-password")
	public String resetPassword( @Valid @ModelAttribute ResetPasswordDTO dto, Model model, BindingResult result,
			RedirectAttributes redirectAttributes) {

		List<Applicant> applicants;
		final Pattern PWD_PATTERN = Pattern.compile(SecurityConstants.PASSWORD_REGEX);
		
		if (result.hasErrors()) {
	        return "applicant/reset-password";
	    }

		// WITH EMAIL
		if (dto.getEmail() != null && !dto.getEmail().trim().isEmpty()) {

			applicants = applicantRepository.findDuplicateWithEmail(dto.getFirstName().trim(), dto.getDateOfBirth(),
					dto.getPhoneNumber().trim(), dto.getEmail().trim());

		} else {

			applicants = applicantRepository.findDuplicateWithoutEmail(dto.getFirstName().trim(), dto.getDateOfBirth(),
					dto.getPhoneNumber().trim());
		}

		// NOT FOUND
		if (applicants.isEmpty()) {

			model.addAttribute("errorMessage", "Applicant details not found.");

			return "applicant/reset-password";
		}

		// DUPLICATE FOUND
		if (applicants.size() > 1) {

			model.addAttribute("errorMessage", "Multiple applicants found with same details. Please contact support.");

			return "applicant/reset-password";
		}

		Applicant applicant = applicants.get(0);

		User user = applicant.getUser();

		if (user == null) {

			model.addAttribute("errorMessage", "No user account linked with applicant.");

			return "applicant/reset-password";
		}

		if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {

			model.addAttribute("errorMessage", "Passwords do not match.");

			return "applicant/reset-password";
		}

		// CHANGE PASSWORD
		if (!PWD_PATTERN.matcher(dto.getNewPassword()).matches()) {
			model.addAttribute("errorMessage",
					"Password must have one special character, one capital, one special character");
		}

		user.setPassword(passwordEncoder.encode(dto.getNewPassword()));

		userRepository.save(user);

		redirectAttributes.addFlashAttribute("successMessage", "Password reset successful. Please login.");

		return "redirect:/login";
	}
}