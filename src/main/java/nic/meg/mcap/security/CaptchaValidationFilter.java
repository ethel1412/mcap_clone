package nic.meg.mcap.security;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CaptchaValidationFilter extends OncePerRequestFilter {

	private static final String SESSION_CAPTCHA_KEY = "captchaText";
	private static final String CAPTCHA_PARAM = "captcha";

	private static final List<String> PROTECTED_URIS = Arrays.asList("/submit", "/login", "/login/process",
			"/applicants/register", "/forgot-password");

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String method = request.getMethod();
		String uri = request.getRequestURI();

		boolean isProtected = "POST".equalsIgnoreCase(method) && PROTECTED_URIS.stream().anyMatch(uri::endsWith);

		if (isProtected) {
			String sessionCaptcha = (request.getSession(false) != null)
					? (String) request.getSession(false).getAttribute(SESSION_CAPTCHA_KEY)
					: null;
			String userCaptcha = request.getParameter(CAPTCHA_PARAM);

			if (sessionCaptcha == null || userCaptcha == null || !sessionCaptcha.equalsIgnoreCase(userCaptcha.trim())) {

				// ================= REGISTER =================

				if (uri.endsWith("/applicants/register")) {

					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

					response.setContentType("application/json");

					response.getWriter().write("""
							{
							    "errorCode":"INVALID_CAPTCHA",
							    "message":"Invalid captcha"
							}
							""");

				}

				// ================= LOGIN AJAX =================

				else if (uri.endsWith("/login/process")) {

					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

					response.setContentType("application/json");

					response.getWriter().write("""
							{
							    "status":"INVALID_CAPTCHA",
							    "message":"Invalid captcha"
							}
							""");
				}

				// ================= NORMAL FORM PAGES =================

				else {

					String redirectUri = uri.endsWith("/submit") ? "/institute-form?error=captcha"
							: uri + "?error=captcha";

					response.sendRedirect(request.getContextPath() + redirectUri);
				}

				return;
			}

			if (request.getSession(false) != null) {
				request.getSession(false).removeAttribute(SESSION_CAPTCHA_KEY);
			}
		}

		filterChain.doFilter(request, response);
	}
}
