package nic.meg.mcap.exception;

import org.springframework.security.core.AuthenticationException;

public class OtpRequiredException extends AuthenticationException {

	/**
	 *
	 */
	private static final long serialVersionUID = 7840973287629978112L;

	public OtpRequiredException(String msg) {
		super(msg);
	}
}