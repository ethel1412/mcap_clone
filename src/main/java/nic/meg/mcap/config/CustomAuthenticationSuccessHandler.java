package nic.meg.mcap.config;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nic.meg.mcap.entities.User;
import nic.meg.mcap.repositories.UserRepository;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

	private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationSuccessHandler.class);

	@Autowired
	private UserRepository userRepository;

	@Override
	@Transactional
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {

		String username = authentication.getName();
		String referer = request.getHeader("Referer");

		// 1. Check if the user has the Institute Role
		boolean isInstitute = authentication.getAuthorities().stream()
				.anyMatch(authority -> "ROLE_INSTITUTE".equals(authority.getAuthority()));

		// 2. SECURITY CHECK: Validate Login Source
		if (referer != null && referer.contains("/institute-login")) {
			if (!isInstitute) {
				// Instead, manually clear the context and the session attribute:
				SecurityContextHolder.clearContext();
				if (request.getSession(false) != null) {
					request.getSession()
							.removeAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
				}

				// Redirect back to login with error
				response.sendRedirect(request.getContextPath() + "/institute-login?error");
				return;
			}
		}

		// 3. Institute Password Change Check
		if (isInstitute) {
			User user = userRepository.findByUsername(username)
					.orElseThrow(() -> new RuntimeException("User not found: " + username));

			if (user.isPasswordChangeRequired()) {
				request.getSession().setAttribute("username", username);
				response.sendRedirect(request.getContextPath() + "/change-password-form");
				return;
			}
		}

		// 4. Default role-based landing pages
		Map<String, String> ROLE_LANDING_PAGE = Map.of("ROLE_ADMIN", "/admin/dashboard", "ROLE_APPLICANT",
				"/applicants/dashboard", "ROLE_INSTITUTE", "/institute-dashboard", "ROLE_CONTROLLER",
				"/control-panel/dashboard");

		String targetUrl = authentication.getAuthorities().stream()
				.map(authority -> ROLE_LANDING_PAGE.get(authority.getAuthority())).filter(Objects::nonNull).findFirst()
				.orElse("/");

		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write("""
				{
				    "status":"LOGIN_SUCCESS",
				    "redirectUrl":"%s"
				}
				""".formatted(request.getContextPath() + targetUrl));
	}
}