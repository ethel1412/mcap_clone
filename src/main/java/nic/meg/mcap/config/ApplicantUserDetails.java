package nic.meg.mcap.config;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import nic.meg.mcap.entities.Applicant;

/**
 * Adapts Applicant to Spring Security's UserDetails for OTP-based login.
 * Password is null; all account flags are true by default.
 */
public class ApplicantUserDetails implements UserDetails {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private final Applicant applicant;

	public ApplicantUserDetails(Applicant applicant) {
		this.applicant = applicant;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return Collections.singletonList(new SimpleGrantedAuthority("ROLE_APPLICANT"));
	}

	@Override
	public String getPassword() {
		// OTP flow is passwordless
		return null;
	}

	@Override
	public String getUsername() {
		// Use mobile as the "username" identity
		return applicant.getApplicantNo();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	/** Convenience accessor if you need the full entity later. */
	public Applicant getApplicant() {
		return applicant;
	}
}
