package nic.meg.mcap.services.impl;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;

import nic.meg.mcap.dto.request.OTPRequestDTO;
import nic.meg.mcap.dto.response.SmsResponse;
import nic.meg.mcap.entities.OtpToken;
import nic.meg.mcap.enums.OtpPurpose;
import nic.meg.mcap.enums.OtpStatus;
import nic.meg.mcap.exception.InvalidIdentifierException;
import nic.meg.mcap.repositories.OtpTokenRepository;
import nic.meg.mcap.services.OtpService;
import nic.meg.mcap.utils.SmsSender;
import nic.meg.mcap.utils.Validator;

@Service
public class OtpServiceImpl implements OtpService {

	private final OtpTokenRepository repo;
	private static final SecureRandom RNG = new SecureRandom();
	private static final Duration OTP_TTL = Duration.ofMinutes(5);
	private static final int MAX_ATTEMPTS = 5;
	private static final int RATE_LIMIT_COUNT = 5;
	private static final Duration RATE_LIMIT_WINDOW = Duration.ofMinutes(15);
	private static final Logger logger = LoggerFactory.getLogger(OtpServiceImpl.class);

	public OtpServiceImpl(OtpTokenRepository repo) {
		this.repo = repo;
	}

	@Autowired
	private SmsSender smsSender;

	@Override
	@Transactional
	public SmsResponse generateOtp(OTPRequestDTO otpDTO) {

		String validIdentifier = "";

		boolean validMobile = Validator.isMobile(otpDTO.getPhoneNumber());

		boolean validEmail = otpDTO.getEmail() != null && !otpDTO.getEmail().isBlank()
				&& Validator.isEmail(otpDTO.getEmail());

		if (!validMobile && !validEmail) {
			throw new InvalidIdentifierException("Invalid mobile number or email");
		}

		// validIdentifier = "+" + otpDTO.getIdentifier();
		if (otpDTO.getPhoneNumber() != null && Validator.isCountryCode(otpDTO.getCountryCode())) {
			validIdentifier = otpDTO.getCountryCode() + otpDTO.getPhoneNumber();
		}

		Instant windowStart = Instant.now().minus(RATE_LIMIT_WINDOW);

		long issued = repo.countByIdentifierAndPurposeAndCreatedAtAfter(validIdentifier, otpDTO.getPurpose(),
				windowStart);

		if (issued >= RATE_LIMIT_COUNT) {
			throw new IllegalStateException("Too many OTP requests. Please try again later.");
		}

		// Expire old OTPs
		repo.findByIdentifierAndPurposeAndStatus(validIdentifier, otpDTO.getPurpose(), OtpStatus.ACTIVE)
				.forEach(t -> t.setStatus(OtpStatus.EXPIRED));

		// Generate OTP
		String otp = generate6();
		Instant now = Instant.now();

		// ================= SEND SMS =================
		try {
//			boolean sent = smsSender.sendSms(SmsTemplate.OTP, validIdentifier, otp, "05");
			boolean sent = true;

			if (!sent) {
				return new SmsResponse(false, "SMS failed to send", "SMS_FAILED");
			}

			else {

				OtpToken token = OtpToken.builder().identifier(validIdentifier).code(otp).purpose(otpDTO.getPurpose())
						.status(OtpStatus.ACTIVE).createdAt(now).expiresAt(now.plus(OTP_TTL)).attempts(0)
						.maxAttempts(MAX_ATTEMPTS).build();

				repo.save(token);
				return new SmsResponse(true, "SMS sent successfully", "OTP_SENT");
			}
		} catch (ResourceAccessException e) {
			return new SmsResponse(false, "SMS gateway not reachable", "GATEWAY_DOWN");

		} catch (HttpStatusCodeException e) {
			return new SmsResponse(false, "SMS gateway error: " + e.getStatusCode(), "GATEWAY_ERROR");

		} catch (Exception e) {
			return new SmsResponse(false, "Failed to send otp", "UNKNOWN_ERROR");
		}
	}

	@Override
	@Transactional

	public boolean verifyOtp(OTPRequestDTO otpDTO) {
		// ✅ STEP 1: Correct validation
		if (!Validator.isMobile(otpDTO.getPhoneNumber()) || !Validator.isCountryCode(otpDTO.getCountryCode())) {
			throw new InvalidIdentifierException("Invalid mobile number.");
		}

		// ✅ Normalize identifier (IMPORTANT)
		String identifier = otpDTO.getCountryCode().trim() + otpDTO.getPhoneNumber().trim();
		// ✅ STEP 2: Fetch latest OTP (remove ACTIVE filter)
		Optional<OtpToken> opt = repo.findTopByIdentifierAndPurposeOrderByCreatedAtDesc(identifier,
				otpDTO.getPurpose());

		if (opt.isEmpty()) {
			return false;
		}

		OtpToken t = opt.get();

		// ✅ STEP 4: Handle already used
		if (t.getStatus() == OtpStatus.CONSUMED) {
			return false;
		}

		// ✅ STEP 5: Expired check
		if (Instant.now().isAfter(t.getExpiresAt())) {
			t.setStatus(OtpStatus.EXPIRED);
			repo.save(t);
			return false;
		}

		// ✅ STEP 6: Attempts check
		if (t.getAttempts() >= t.getMaxAttempts()) {
			t.setStatus(OtpStatus.BLOCKED);
			repo.save(t);
			return false;
		}

		// ✅ STEP 7: Compare OTP
		String userOtp = otpDTO.getOtp() != null ? otpDTO.getOtp().trim() : "";

		if (t.getCode().equals(userOtp)) {

			t.setStatus(OtpStatus.CONSUMED);
			t.setConsumedAt(Instant.now());
			repo.save(t);

			return true;

		} else {

			t.setAttempts(t.getAttempts() + 1);

			if (t.getAttempts() >= t.getMaxAttempts()) {
				t.setStatus(OtpStatus.BLOCKED);
			}

			repo.save(t);
			return false;
		}
	}

	@Override
	@Transactional
	public void clearOtp(String mobile) {
		String m = normalizeMobile(mobile);
		repo.findByIdentifierAndPurposeAndStatus(m, OtpPurpose.APPLICANT_LOGIN, OtpStatus.ACTIVE)
				.forEach(t -> t.setStatus(OtpStatus.EXPIRED));
	}

	@Scheduled(cron = "0 0 * * * *") // hourly
	@Transactional
	public void cleanupExpired() {
		repo.deleteByStatusAndExpiresAtBefore(OtpStatus.EXPIRED, Instant.now().minus(Duration.ofDays(1)));
	}

	private static String generate6() {
		int n = RNG.nextInt(900_000) + 100_000;
		return Integer.toString(n);
	}

	private static String normalizeMobile(String s) {
		if (s == null)
			return "";
		String digits = s.replaceAll("\\D", "");
		return digits.length() > 10 ? digits.substring(digits.length() - 10) : digits; // e.g., IN last 10
	}
}
