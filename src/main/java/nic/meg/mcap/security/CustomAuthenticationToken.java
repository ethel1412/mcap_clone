package nic.meg.mcap.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

public class CustomAuthenticationToken extends UsernamePasswordAuthenticationToken {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private final String dob;

	public CustomAuthenticationToken(Object principal, Object credentials, String dob) {
		super(principal, credentials);
		this.dob = dob;
	}

	public String getDob() {
		return dob;
	}
}