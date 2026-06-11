package nic.meg.mcap.security;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.thymeleaf.extras.springsecurity6.dialect.SpringSecurityDialect;

import jakarta.servlet.http.HttpServletResponse;
import nic.meg.mcap.config.CustomAuthenticationProvider;
import nic.meg.mcap.config.CustomAuthenticationSuccessHandler;
import nic.meg.mcap.config.NoCacheFilter;
import nic.meg.mcap.exception.OtpRequiredException;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
	private final CustomAuthenticationProvider customAuthProvider;
	private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
	private final RateLimitFilter rateLimitFilter;
	private final AuthenticationConfiguration authenticationConfiguration;
	private final NoCacheFilter noCacheFilter;

	public SecurityConfig(@Lazy CustomAuthenticationProvider customAuthProvider,
			CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler, RateLimitFilter rateLimitFilter,
			AuthenticationConfiguration authenticationConfiguration, NoCacheFilter noCacheFilter) {

		this.customAuthProvider = customAuthProvider;
		this.customAuthenticationSuccessHandler = customAuthenticationSuccessHandler;
		this.rateLimitFilter = rateLimitFilter;
		this.authenticationConfiguration = authenticationConfiguration;
		this.noCacheFilter = noCacheFilter;
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	SecurityContextRepository httpSessionSecurityContextRepository() {
		HttpSessionSecurityContextRepository repository = new HttpSessionSecurityContextRepository();

		return repository;
	}

	@Bean
	CaptchaValidationFilter captchaValidationFilter() {
		return new CaptchaValidationFilter();
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		CustomAuthenticationFilter customFilter = new CustomAuthenticationFilter();

		customFilter.setAuthenticationManager(getAuthenticationManager());
		customFilter.setFilterProcessesUrl("/login/process");
		customFilter.setAuthenticationSuccessHandler(customAuthenticationSuccessHandler);

//		customFilter.setAuthenticationFailureHandler((request, response, exception) -> {
//			response.sendRedirect(request.getContextPath() + "/login?error");
//		});

		customFilter.setAuthenticationFailureHandler((request, response, exception) -> {

			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");

			// ================= OTP REQUIRED =================

			if (exception instanceof OtpRequiredException) {

				response.setStatus(HttpServletResponse.SC_OK);

				response.getWriter().write("""
							{
							  "status":"OTP_REQUIRED",
							  "message":"OTP sent successfully"
							}
						""");

				return;
			}

			// ================= LOGIN FAILED =================

			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

			response.getWriter().write("""
						{
						  "status":"LOGIN_FAILED",
						  "message":"Invalid credentials"
						}
					""");
		});

		http.csrf(csrf -> csrf.ignoringRequestMatchers("/applicants/payment/webhook", "/otp/**"))
				.cors(cors -> cors.configurationSource(corsConfigurationSource()))
				.addFilterBefore(noCacheFilter, UsernamePasswordAuthenticationFilter.class)
				.addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)

				.addFilterBefore(captchaValidationFilter(), UsernamePasswordAuthenticationFilter.class)

				.addFilterAt(customFilter, UsernamePasswordAuthenticationFilter.class)

				.authenticationProvider(customAuthProvider)

				.securityContext(securityContext -> securityContext
						.securityContextRepository(httpSessionSecurityContextRepository()))

				.authorizeHttpRequests(auth -> auth.requestMatchers(HttpMethod.TRACE, "/**").denyAll()
						.requestMatchers("/", "/master/**", "/user-management/**", "/institute-form", "/submit",
								"/districts", "/blocks", "/login", "/applicants/**", "/register",
								"/applicant-registration", "/error", "/error/**", "/static/**", "/resources/**",
								"/.*\\.png", "/.*\\.svg", "/.*\\.css", "/.*\\.js", "/.*\\.min.js", "/.*\\.ico",
								"/webfonts/**", "/css/**", "/js/**", "/assets/**", "/images/**", "/webjars/**",
								"/api/**", "/data/**", "/stream-data/**", "/institute/status", "/change-password-form",
								"/admission-guidelines", "/contact-us", "/about", "/notifications",
								"/participating-institutes", "/create-order", "/reset-password",
								"/applicants/payment/payment-status", "/about", "/faq")
						.permitAll()

						.requestMatchers(HttpMethod.POST, "/applicants/payment/webhook").permitAll()

						.requestMatchers(HttpMethod.POST, "/captcha/get-captcha", "/key/get-publickey").permitAll()

						.requestMatchers("/controller/admissions/**", "/roadmap", "/step-template-data/templates/**",
								"/schedule-data/**", "/mis/**")
						.hasAnyRole("ADMIN", "CONTROLLER")

						.requestMatchers(HttpMethod.POST, "/otp/**").permitAll()

						.requestMatchers("/profile/**").authenticated()

						.requestMatchers(HttpMethod.POST, "/user-management/data/**", "/role-data/**", "/submit")
						.hasRole("ADMIN")

						.requestMatchers("/programme-requests/**", "/department-requests/**", "/admission-window/**",
								"/institute/**")
						.hasAnyRole("INSTITUTE", "CONTROLLER")

						.requestMatchers("/admin/dashboard", "/step-template-data/**").hasRole("ADMIN")

						.requestMatchers("/institute-dashboard", "/institute-registration-fee/**",
								"/seat-reservations/page/**", "/seat-reservations/**", "/api/admission-window/**",
								"/manage-programmes-data/**", "/institute-pages/**", "/institute/programmes/**",
								"/api/institute/**", "/institute/**", "/stream-data/**", "/programme-data/**",
								"/institute-departments/**", "/programmes-offered/**", "/institute/status",
								"/institute/correction/**", "/institute/view-applications",
								"/institute-notifications/**", "/institute/profile", "/institute/profile/update",
                                "/institute/seat-fee/**")
						.hasRole("INSTITUTE")

						.requestMatchers("/control-panel/**", "/admission-criteria/page/**",
								"/admission-criteria/data/**", "/merit-list/**", "/controller/**",
								"/controller/seat-approvals/**", "/eligibility/data/**", "/seat-allotment/page/**",
								"/seat-allotment-data/**", "/mis/**")
						.hasRole("CONTROLLER")

						.requestMatchers("/role-data/**").authenticated()

						.requestMatchers("/applicant/**").hasRole("APPLICANT")

						.requestMatchers("/menu", "/user-management/**", "/dashboard", "/admin/**", "/secure/**",
								"/role-management/**", "/schedule-management/**", "institute-departments/**",
								"/programmes-offered/**", "/semesters/data/**", "/subject-assignments/data/**",
								"/programme-subjects/**", "/subject-data/**", "/seat-matrix/**", "/admission-window/**",
								"/admission-criterion/data/**", "/programmes-offered/data/**",
								"/seat-reservations/page/**", "/seat-reservations/**", "/grievances/**")
						.authenticated()

						.anyRequest().denyAll())

				.exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, authException) -> {

					String uri = request.getRequestURI();

					if (uri.equals("/change-password-form") || isProtectedPath(uri)) {

						response.sendRedirect(request.getContextPath() + "/login");

					} else {
						response.sendError(HttpServletResponse.SC_NOT_FOUND);
					}
				}))

				.formLogin(form -> form.loginPage("/login").loginProcessingUrl("/login/process").permitAll()

						.failureHandler((request, response, authException) -> {

							String referer = request.getHeader("Referer");

							if (referer != null && referer.contains("/institute-login")) {

								response.sendRedirect(request.getContextPath() + "/institute-login?error");

							} else if (referer != null && referer.contains("/applicants/login")) {

								response.sendRedirect(request.getContextPath() + "/applicants/login?error");

							} else {

								response.sendRedirect(request.getContextPath() + "/login?error");
							}
						})

						.successHandler(customAuthenticationSuccessHandler))

				.logout(logout -> logout.logoutUrl("/logout").invalidateHttpSession(true).clearAuthentication(true)
						.deleteCookies("JSESSIONID").logoutSuccessUrl("/"))

				.sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
						.invalidSessionUrl("/").sessionFixation(session -> session.changeSessionId()).maximumSessions(1)
						.expiredUrl("/login?expired"))

				.headers(headers -> headers.addHeaderWriter((request, response) -> {
					headers.cacheControl(Customizer.withDefaults());

					response.setHeader("Cross-Origin-Opener-Policy", "same-origin");

					response.setHeader("Cross-Origin-Embedder-Policy", "credentialless");

					response.setHeader("Cross-Origin-Resource-Policy", "same-origin");
				})

						.referrerPolicy(referrer -> referrer
								.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))

						.xssProtection(
								xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))

						.httpStrictTransportSecurity(
								hsts -> hsts.includeSubDomains(true).preload(true).maxAgeInSeconds(31536000))

						.frameOptions(frame -> frame.sameOrigin())

						.cacheControl(Customizer.withDefaults())

						.contentTypeOptions(Customizer.withDefaults())

						.contentSecurityPolicy(csp ->
//                        csp.policyDirectives(
//                            "default-src 'self'; " +
//                            "script-src 'self' https://cdn.ux4g.gov.in https://cdn.jsdelivr.net https://sdk.cashfree.com https://*.cashfree.com; " +
//                            "style-src 'self' https://cdn.ux4g.gov.in https://cdn.jsdelivr.net https://fonts.googleapis.com https://cdnjs.cloudflare.com; " +
//                            "font-src 'self' https://fonts.gstatic.com https://cdnjs.cloudflare.com https://cdn.jsdelivr.net https://cdn.ux4g.gov.in; " +
//                            "img-src 'self' data: https://*.cashfree.com; " +
//                            "connect-src 'self' https://sandbox.cashfree.com https://api.cashfree.com https://*.cashfree.com; " +
//                            "frame-src 'self' https://*.cashfree.com; " +
//                            "object-src 'none'; " +
//                            "base-uri 'self'; " +
//                            "form-action 'self'; " +
//                            "frame-ancestors 'self';"
//                        )
//						csp.policyDirectives("default-src 'self'; " + "script-src 'self' https://cdn.ux4g.gov.in;"
//								+ "style-src 'self' https://cdn.ux4g.gov.in; "
//								+ "font-src 'self' https://cdn.ux4g.gov.in; " + "img-src 'self' data: https:; "
//								+ "connect-src 'self'; " + "object-src 'none'; " + "base-uri 'self'; "

//								+ "form-action 'self'; " + "frame-ancestors 'none'; " + "upgrade-insecure-requests; "
//								+ "block-all-mixed-content;"))

						csp.policyDirectives("default-src 'self'; " +

								"script-src 'self' https://sdk.cashfree.com; " +

								"style-src 'self' " + "https://cdn.ux4g.gov.in " + "https://fonts.googleapis.com; " +

								"font-src 'self' data: " + "https://fonts.gstatic.com " + "https://cdn.ux4g.gov.in; " +

								"connect-src 'self' https://sandbox.cashfree.com https: ;" +

								"frame-src 'self'; " +

								"form-action 'self' https://sandbox.cashfree.com; " +

								"img-src 'self' data: https:; " +

								"object-src 'none'; " + "base-uri 'self';" + "frame-ancestors 'none';"))
						.referrerPolicy(ref -> ref.policy(ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)));

		return http.build();
	}

	@Bean
	SpringSecurityDialect securityDialect() {
		return new SpringSecurityDialect();
	}

	private boolean isProtectedPath(String uri) {

		List<String> protectedPaths = List.of("/admin/dashboard", "/admin", "/secure", "/home", "/institute-dashboard");

		return protectedPaths.stream().anyMatch(uri::startsWith);
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource() {

		return request -> {

			CorsConfiguration config = new CorsConfiguration();

			config.setAllowedOrigins(List.of("https://megdemo1.nic.in", "http://localhost", "http://10.179.2.80",
					"https://mcap.nic.in"));

			config.setAllowedMethods(List.of("POST", "GET", "PUT", "DELETE", "OPTIONS"));

			config.setAllowedHeaders(
					List.of("Authorization", "Content-Type", "X-Requested-With", "Accept", "X-XSRF-TOKEN"));

			config.setAllowCredentials(true);

			return config;
		};
	}

	private AuthenticationManager getAuthenticationManager() {

		try {
			return authenticationConfiguration.getAuthenticationManager();

		} catch (Exception e) {

			throw new IllegalStateException("Failed to obtain AuthenticationManager", e);
		}
	}
}