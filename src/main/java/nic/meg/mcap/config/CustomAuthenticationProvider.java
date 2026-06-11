package nic.meg.mcap.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import jakarta.validation.Valid;
import nic.meg.mcap.enums.LoginType;
import nic.meg.mcap.exception.OtpRequiredException;
import nic.meg.mcap.repositories.ApplicantRepository;
import nic.meg.mcap.security.CustomAuthenticationToken;
import nic.meg.mcap.services.AuthenticationService;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

	private final AuthenticationService authService;
	private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationProvider.class);

	public CustomAuthenticationProvider(AuthenticationService authService, ApplicantRepository applicantRepo) {
		this.authService = authService;
	}

	@Override
	public Authentication authenticate(@Valid Authentication authentication) {

		CustomAuthenticationToken token = (CustomAuthenticationToken) authentication;
		String username = token.getName();
		String password = (String) token.getCredentials();
		String dob = token.getDob();

		try {

			UserDetails userDetails = authService.isValidUser(username, password, dob, LoginType.STANDARD);

			boolean requiresOtp = userDetails.getAuthorities().stream().anyMatch(auth ->

			auth.getAuthority().equals("ROLE_ADMIN")

					|| auth.getAuthority().equals("ROLE_CONTROLLER"));

			if (requiresOtp) {

				throw new OtpRequiredException("OTP verification required");
			}

			return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

		} catch (OtpRequiredException ex) {

			throw ex;

		} catch (BadCredentialsException ex) {

			throw new BadCredentialsException("Authentication failed");

		} catch (Exception ex) {
			throw new BadCredentialsException("Authentication failed");
		}
	}

	@Override
	public boolean supports(Class<?> authenticationType) {
		return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authenticationType);
	}
}