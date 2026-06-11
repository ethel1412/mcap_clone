package nic.meg.mcap.controllers;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nic.meg.mcap.dto.request.OTPRequestDTO;
import nic.meg.mcap.entities.User;
import nic.meg.mcap.repositories.UserRepository;
import nic.meg.mcap.services.OtpService;

@RestController
@RequestMapping("/otp")
@RequiredArgsConstructor
public class OtpController {

	private final OtpService otpService;
	private final UserRepository userRepository;

	@PostMapping(value = "/send-otp", produces = "application/json")
	@ResponseBody
	public ResponseEntity<?> sendOtp(@Valid @RequestBody OTPRequestDTO otpDTO, HttpServletRequest request) {

		String expectedCaptcha = (String) request.getSession().getAttribute("captchaText");

		if (expectedCaptcha == null || otpDTO.getCaptcha() == null
				|| !expectedCaptcha.equalsIgnoreCase(otpDTO.getCaptcha())) {

			return ResponseEntity.badRequest()
					.body(Map.of("success", false, "message", "Invalid captcha", "errorCode", "INVALID_CAPTCHA"));
		}

		request.getSession().removeAttribute("captchaText");
		otpService.generateOtp(otpDTO);
		return ResponseEntity.ok(Map.of("success", true, "message", "SMS sent successfully", "errorCode", "OTP_SENT"));
	}

	@PostMapping("/verify-otp")
	public ResponseEntity<?> verifyOtp(@Valid @RequestBody OTPRequestDTO otpDTO, HttpServletRequest request) {

		// ✅ 1. Validate CAPTCHA first
		String expectedCaptcha = (String) request.getSession().getAttribute("captchaText");

		if (expectedCaptcha == null || otpDTO.getCaptcha() == null
				|| !expectedCaptcha.equalsIgnoreCase(otpDTO.getCaptcha())) {

			return ResponseEntity.badRequest().body(Map.of("message", "Invalid captcha"));
		}

		// ✅ 2. Prevent reuse
		request.getSession().removeAttribute("captchaText");

		boolean isValid = otpService.verifyOtp(otpDTO);

		if (isValid) {
			return ResponseEntity.ok(Map.of("status", "OTP_VERIFIED"));
		}
		return ResponseEntity.badRequest().body(Map.of("status", "INVALID_OTP", "message", "Invalid or expired OTP"));
	}

	@PostMapping("/verify-login-otp")
	public ResponseEntity<?> verifyLoginOtp(@Valid @RequestBody OTPRequestDTO otpDTO, HttpServletRequest request) {

		// ================= SESSION CHECK =================

		HttpSession session = request.getSession(false);

		if (session == null) {

			return ResponseEntity.badRequest()
					.body(Map.of("status", "SESSION_EXPIRED", "message", "Login session expired"));
		}

		String username = (String) session.getAttribute("PENDING_LOGIN_USER");

		String phoneNumber = (String) session.getAttribute("PENDING_OTP_PHONE");

		if (username == null || phoneNumber == null) {

			return ResponseEntity.badRequest()
					.body(Map.of("status", "SESSION_EXPIRED", "message", "Login session expired"));
		}

		// ================= BUILD VERIFY DTO =================

		OTPRequestDTO verifyDto = new OTPRequestDTO();

		verifyDto.setCountryCode("+91");

		verifyDto.setPhoneNumber(phoneNumber);

		verifyDto.setOtp(otpDTO.getOtp());

		verifyDto.setPurpose(otpDTO.getPurpose());

		// ================= VERIFY OTP =================

		boolean isValid = otpService.verifyOtp(verifyDto);

		if (!isValid) {

			return ResponseEntity.badRequest()
					.body(Map.of("status", "INVALID_OTP", "message", "Invalid or expired OTP"));
		}

		// ================= FETCH USER =================

		User user = userRepository.findByUsername(username).orElseThrow();

		// ================= SESSION FIXATION =================

		request.changeSessionId();

		// IMPORTANT:
		// get fresh session reference

		session = request.getSession(false);

		// ================= AUTHENTICATE =================

		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null,
				user.getAuthorities());

		SecurityContext context = SecurityContextHolder.createEmptyContext();

		context.setAuthentication(auth);

		SecurityContextHolder.setContext(context);

		// ================= STORE CONTEXT =================

		session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

		// ================= CLEANUP =================

		session.removeAttribute("PENDING_LOGIN_USER");

		session.removeAttribute("PENDING_OTP_PHONE");

		// ================= ROLE REDIRECT =================

		String redirectUrl = "/admin/dashboard";

		if (user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CONTROLLER"))) {

			redirectUrl = "/control-panel/dashboard";
		}

		// ================= SUCCESS =================

		return ResponseEntity.ok(Map.of("status", "LOGIN_SUCCESS", "redirectUrl", redirectUrl));
	}

	@PostMapping("/clear-otp")
	public ResponseEntity<String> clearOtp(@RequestParam String applicationNo) {
		otpService.clearOtp(applicationNo);
		return ResponseEntity.ok("OTP cleared for applicationNo: " + applicationNo);
	}
}
