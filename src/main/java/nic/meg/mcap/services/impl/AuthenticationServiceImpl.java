package nic.meg.mcap.services.impl;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import nic.meg.mcap.dto.request.OTPRequestDTO;
import nic.meg.mcap.dto.response.SmsResponse;
import nic.meg.mcap.entities.Applicant;
import nic.meg.mcap.entities.LoginActivity;
import nic.meg.mcap.entities.User;
import nic.meg.mcap.enums.LoginType;
import nic.meg.mcap.enums.OtpPurpose;
import nic.meg.mcap.repositories.ApplicantRepository;
import nic.meg.mcap.repositories.LoginActivitieRepository;
import nic.meg.mcap.repositories.UserRepository;
import nic.meg.mcap.services.AuthenticationService;
import nic.meg.mcap.services.OtpService;
import nic.meg.mcap.utils.RSAUtil;
import nic.meg.mcap.utils.SecurityConstants;
import nic.meg.mcap.utils.Validator;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

	@Autowired
	private UserRepository repository;

	@Autowired
	private ApplicantRepository applicantRepository;

	@Autowired
	private ModelMapper modelMapper;

	@Autowired
	private LoginActivitieRepository loginActivities;

	@Autowired
	private HttpServletRequest request;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private OtpService otpService;

	@Autowired
	private RSAUtil rsaUtil;

	private static final Pattern PWD_PATTERN = Pattern.compile(SecurityConstants.PASSWORD_REGEX);

	@Override
	public UserDetails isValidUser(String username, String rawOrEncryptedPassword, String dob, LoginType loginType) {

		String submittedPassword;
		LocalDate parsedDob = null;
		User user;
		boolean isOTPRequired = false;

		OTPRequestDTO otpDTO = new OTPRequestDTO();
		otpDTO.setCountryCode("+91");
		otpDTO.setPurpose(OtpPurpose.APPLICANT_LOGIN);

		LoginActivity loginActivity = LoginActivity.builder().usernameAttempt(username)
				.ipAddress(request.getRemoteAddr()).build();

		try {
			// 🔐 Step 1: Decrypt & parse (DOB optional)
			if (loginType == LoginType.STANDARD) {

				submittedPassword = rsaUtil.decrypt(rawOrEncryptedPassword);

				if (dob != null && !dob.isBlank()) {
					String decryptedDob = rsaUtil.decrypt(dob);
					try {
						parsedDob = LocalDate.parse(decryptedDob); // yyyy-MM-dd
					} catch (DateTimeParseException ex) {
						throw new BadCredentialsException("Invalid credentials");
					}
				}

			} else {
				submittedPassword = rawOrEncryptedPassword;
			}

			// 🔍 Step 2: Username validation
			if (!Validator.isEmail(username) && !Validator.isMobile(username) && !Validator.isValidUsername(username)) {
				throw new BadCredentialsException("Invalid username");
			}

			// 🔐 Step 3: Password format validation
			if (loginType == LoginType.STANDARD) {
				if (!PWD_PATTERN.matcher(submittedPassword).matches()) {
					throw new BadCredentialsException("Invalid password format or complexity.");
				}
			}

			// 👤 Step 4: Fetch user (DOB optional logic)
			if (Validator.isMobile(username)) {

				Applicant applicant;
				if (parsedDob != null) {
					applicant = applicantRepository.findByPhoneNumberAndDateOfBirth(username, parsedDob)
							.orElseThrow(() -> new BadCredentialsException("Invalid username or password"));
				} else {
					applicant = applicantRepository.findByPhoneNumber(username)
							.orElseThrow(() -> new BadCredentialsException("Invalid username or password"));
				}

				user = repository.findByUserId(applicant.getUser().getUserId())
						.orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

				user.setUsername(applicant.getApplicantNo());

			} else if (Validator.isEmail(username)) {

				Applicant applicant;

				if (parsedDob != null) {
					applicant = applicantRepository.findByEmailAndDateOfBirth(username, parsedDob)
							.orElseThrow(() -> new BadCredentialsException("Invalid username or password"));
				} else {
					applicant = applicantRepository.findByEmail(username)
							.orElseThrow(() -> new BadCredentialsException("Invalid username or password"));
				}

				user = repository.findByUsername(applicant.getPhoneNumber())
						.orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

				user.setUsername(applicant.getApplicantNo());

			} else {

				user = repository.findByUsername(username)
						.orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
				otpDTO.setPhoneNumber(user.getPhoneNumber());
				isOTPRequired = true;
			}

			// 🔑 Step 5: Password match
			if (!passwordEncoder.matches(submittedPassword, user.getPassword())) {
				throw new BadCredentialsException("Invalid credentials");
			}

			if (isOTPRequired) {
				SmsResponse smsResponse = otpService.generateOtp(otpDTO);

				if (!smsResponse.getSuccess()) {
					throw new BadCredentialsException(smsResponse.getMessage());
				}
			}
			// 🚫 Step 6: Account checks
			if (!user.isEnabled()) {
				throw new BadCredentialsException("Your account is pending administrator approval.");
			}

			if (!user.isAccountNonLocked() || !user.isAccountNonExpired() || !user.isCredentialsNonExpired()) {
				throw new BadCredentialsException("Account is disabled, locked, or expired.");
			}

			// ✅ Success
			loginActivities.save(loginActivity.toBuilder().isSuccess(true).build());
			request.getSession().setAttribute("PENDING_LOGIN_USER", user.getUsername());

			request.getSession().setAttribute("PENDING_OTP_PHONE", otpDTO.getPhoneNumber());
			return modelMapper.map(user, User.class);

		} catch (BadCredentialsException ex) {
			loginActivities.save(loginActivity.toBuilder().isSuccess(false).build());
			throw ex;

		} catch (Exception ex) {
			loginActivities.save(loginActivity.toBuilder().isSuccess(false).build());
			throw new BadCredentialsException("Invalid credentials");
		}
	}

	@Override
	public Long getCurrentUserLoginActivityId() {

		var auth = SecurityContextHolder.getContext().getAuthentication();

		if (auth == null || !auth.isAuthenticated()) {
			throw new org.springframework.security.authentication.AuthenticationCredentialsNotFoundException(
					"User not authenticated");
		}

		final String currentUserName = auth.getName();

		User currentUser = repository.findByUsername(currentUserName)
				.orElseThrow(() -> new UsernameNotFoundException("Current user not found: " + currentUserName));

		return loginActivities.findByUser_UserIdOrderByTimeDesc(currentUser.getUserId()).stream().findFirst()
				.map(LoginActivity::getId).orElse(null);
	}
}