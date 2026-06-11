package nic.meg.mcap.entities;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nic.meg.mcap.enums.OtpPurpose;
import nic.meg.mcap.enums.OtpStatus;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class OtpToken {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** Normalized mobile number (e.g., last 10 digits for Indian numbers). */
	@Column(nullable = false, length = 20)
	private String identifier;

	/** OTP code (usually 6 digits). */
	@Column(nullable = false, length = 6)
	private String code;

	/** Purpose of this OTP (e.g., APPLICANT_LOGIN). */
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private OtpPurpose purpose;

	/** Current status (ACTIVE, CONSUMED, EXPIRED, BLOCKED). */
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 16)
	private OtpStatus status;

	/** When this OTP was created. */
	@Column(nullable = false)
	private Instant createdAt;

	/** When this OTP will expire. */
	@Column(nullable = false)
	private Instant expiresAt;

	/** When this OTP was consumed successfully. */
	private Instant consumedAt;

	/** Number of failed verification attempts. */
	@Column(nullable = false)
	private int attempts;

	/** Maximum allowed verification attempts. */
	@Column(nullable = false)
	private int maxAttempts;
}
