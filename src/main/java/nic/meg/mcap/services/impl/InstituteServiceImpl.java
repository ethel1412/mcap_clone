package nic.meg.mcap.services.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import nic.meg.mcap.dto.request.AddressDTO;
import nic.meg.mcap.dto.request.InstituteRequestDTO;
import nic.meg.mcap.dto.request.InstituteStatusRequestDTO;
import nic.meg.mcap.dto.response.InstituteApprovalResponseDTO;
import nic.meg.mcap.dto.response.InstituteResponseDTO;
import nic.meg.mcap.dto.response.InstituteStatusResponseDTO;
import nic.meg.mcap.entities.Address;
import nic.meg.mcap.entities.AffiliationType;
import nic.meg.mcap.entities.Block;
import nic.meg.mcap.entities.District;
import nic.meg.mcap.entities.Institute;
import nic.meg.mcap.entities.ManagementType;
import nic.meg.mcap.entities.Role;
import nic.meg.mcap.entities.State;
import nic.meg.mcap.entities.User;
import nic.meg.mcap.enums.InstituteStatus;
import nic.meg.mcap.enums.OrgOwnerType;
import nic.meg.mcap.repositories.AddressRepository;
import nic.meg.mcap.repositories.AffiliationTypeRepository;
import nic.meg.mcap.repositories.BlockRepository;
import nic.meg.mcap.repositories.DistrictRepository;
import nic.meg.mcap.repositories.InstituteRepository;
import nic.meg.mcap.repositories.ManagementTypeRepository;
import nic.meg.mcap.repositories.RoleRepository;
import nic.meg.mcap.repositories.StateRepository;
import nic.meg.mcap.repositories.UserRepository;
import nic.meg.mcap.services.InstituteService;
import nic.meg.mcap.utils.PasswordGenerator;

@Service
public class InstituteServiceImpl implements InstituteService {

	private static final Logger logger = LoggerFactory.getLogger(InstituteServiceImpl.class);

	@Autowired
	private InstituteRepository instituteRepository;
	@Autowired
	private AffiliationTypeRepository affiliationTypeRepository;
	@Autowired
	private ManagementTypeRepository managementTypeRepository;
	@Autowired
	private StateRepository stateRepository;
	@Autowired
	private DistrictRepository districtRepository;
	@Autowired
	private BlockRepository blockRepository;
	@Autowired
	private AddressRepository addressRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private RoleRepository roleRepository;
	@Autowired
	private PasswordEncoder passwordEncoder;

	@Override
	@Transactional
	public InstituteResponseDTO saveInstitute(InstituteRequestDTO dto) {
		AddressDTO addressDTO = dto.getAddressDTO();
		if (addressDTO == null || dto.getAffiliationTypeId() == null || dto.getManagementTypeId() == null
				|| addressDTO.getStateCode() == null || addressDTO.getDistrictCode() == null
				|| addressDTO.getBlockCode() == null) {
			throw new IllegalArgumentException("Required fields are missing. Please check your submission.");
		}

		Institute institute;
		Address address;

		if (dto.getInstituteId() != null) {
			institute = instituteRepository.findById(dto.getInstituteId())
					.orElseThrow(() -> new IllegalArgumentException(
							"Institute not found for update with ID: " + dto.getInstituteId()));
			address = institute.getAddress();
			if (address == null) {
				address = new Address();
				institute.setAddress(address);
			}
			institute.setStatus(InstituteStatus.PENDING);
			institute.setRejectionReason(null);
			institute.setCorrectionPendingReview(true);
		} else {
			institute = new Institute();
			institute.setInstituteCode(UUID.randomUUID());
			address = new Address();
			institute.setAddress(address);
			institute.setStatus(InstituteStatus.PENDING);
			institute.setRejectionReason(null);
			institute.setCorrectionPendingReview(false);
		}

		State state = stateRepository.findById(addressDTO.getStateCode())
				.orElseThrow(() -> new IllegalArgumentException("Invalid State Code: " + addressDTO.getStateCode()));
		District district = districtRepository.findById(addressDTO.getDistrictCode()).orElseThrow(
				() -> new IllegalArgumentException("Invalid District Code: " + addressDTO.getDistrictCode()));
		Block block = blockRepository.findById(addressDTO.getBlockCode())
				.orElseThrow(() -> new IllegalArgumentException("Invalid Block Code: " + addressDTO.getBlockCode()));
		AffiliationType affiliationType = affiliationTypeRepository.findById(dto.getAffiliationTypeId()).orElseThrow(
				() -> new IllegalArgumentException("Invalid Affiliation Type ID: " + dto.getAffiliationTypeId()));
		ManagementType managementType = managementTypeRepository.findById(dto.getManagementTypeId()).orElseThrow(
				() -> new IllegalArgumentException("Invalid Management Type ID: " + dto.getManagementTypeId()));

		address.setAddressLine1(addressDTO.getAddressLine1());
		address.setAddressLine2(addressDTO.getAddressLine2());
		address.setPincode(addressDTO.getPincode());
		address.setState(state);
		address.setDistrict(district);
		address.setBlock(block);
		address.setUserType("INSTITUTE");
		address.setAddressType("PERMANENT");

		institute.setInstituteName(dto.getInstituteName());
		institute.setAISHEId(dto.getAISHEId());
		institute.setYearEstablished(dto.getYearEstablished());
		institute.setBorderDistrictArea(dto.getBorderDistrictArea());
		institute.setUniversityName(dto.getUniversityName());
		institute.setInstitutionHeadDetails(dto.getInstitutionHeadDetails());
		institute.setInstitutionOfficialEmailId(dto.getInstitutionOfficialEmailId());
		institute.setInstitutionOfficialContactNumber(dto.getInstitutionOfficialContactNumber());
		institute.setInstitutionWebsite(dto.getInstitutionWebsite());
		institute.setAffiliationType(affiliationType);
		institute.setManagementType(managementType);

		Institute savedInstitute = instituteRepository.save(institute);

		if (dto.getInstituteId() == null && address.getEntityId() == null) {
			address.setEntityId(savedInstitute.getInstituteCode());
		}
		addressRepository.save(address);
		return new InstituteResponseDTO(savedInstitute);
	}

	@Override
	@Transactional
	public InstituteApprovalResponseDTO updateStatus(Short instituteId, String status, String reason) {
		Institute institute = instituteRepository.findById(instituteId)
				.orElseThrow(() -> new IllegalArgumentException("Institute not found with ID: " + instituteId));

		InstituteStatus newStatus;
		try {
			newStatus = InstituteStatus.valueOf(status.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Invalid status provided: " + status);
		}

		InstituteStatus originalStatus = institute.getStatus();
		institute.setStatus(newStatus);
		String message = "";
		String tempUsername = null;
		String tempPasswordPlaintext = null;

		Optional<User> userOpt = userRepository.findByOrgOwnerTypeAndOrgOwnerId(OrgOwnerType.INSTITUTE, instituteId);

		if (newStatus == InstituteStatus.REJECTED) {
			if (reason == null || reason.trim().isEmpty()) {
				throw new IllegalArgumentException("A reason is required for rejection.");
			}
			institute.setRejectionReason(reason);
			institute.setCorrectionPendingReview(false);
			userOpt.ifPresent(user -> {
				user.setEnabled(false);
				user.setPasswordChangeRequired(false);
				user.setTempPlaintextPassword(null);
				userRepository.save(user);
			});
			message = "Institute application has been rejected successfully.";

		} else if (newStatus == InstituteStatus.CORRECTION_REQUIRED) {
			if (reason == null || reason.trim().isEmpty()) {
				throw new IllegalArgumentException("A reason is required for sending back for correction.");
			}
			institute.setRejectionReason(reason);
			institute.setCorrectionPendingReview(false);
			userOpt.ifPresent(user -> {
				user.setEnabled(false);
				user.setPasswordChangeRequired(false);
				user.setTempPlaintextPassword(null);
				userRepository.save(user);
			});
			message = "Application has been sent back for corrections successfully.";

		} else if (newStatus == InstituteStatus.ACCEPTED) {
			institute.setRejectionReason(null);
			institute.setCorrectionPendingReview(false);

			User user;
			if (userOpt.isPresent()) {
				user = userOpt.get();
				tempUsername = user.getUsername();

			} else {
				String aisheid = institute.getAISHEId();
				if (aisheid == null || aisheid.trim().isEmpty()) {
					throw new IllegalArgumentException(
							"AISHEID is required to create a user account for institute ID: " + instituteId);
				}

				tempUsername = aisheid;

				if (userRepository.existsByUsername(tempUsername)) {
					throw new IllegalArgumentException("User account already exists for AISHEID: " + aisheid);
				}

				Role instituteRole = roleRepository.findByRoleName("INSTITUTE")
						.orElseThrow(() -> new IllegalArgumentException(
								"Role 'INSTITUTE' not found. Please ensure this role exists in your database."));

				user = new User();
				user.setUserCode(UUID.randomUUID());
				user.setUsername(tempUsername);
				user.setOrgOwnerType(OrgOwnerType.INSTITUTE);
				user.setOrgOwnerId(instituteId);
				user.setIsSuperuser(false);
				user.setAccountNonExpired(true);
				user.setAccountNonLocked(true);
				user.setCredentialsNonExpired(true);
				user.setRole(instituteRole);
			}

			tempPasswordPlaintext = PasswordGenerator.generateRandomPassword(12);
			user.setPassword(passwordEncoder.encode(tempPasswordPlaintext));
			user.setEnabled(true);
			user.setPasswordChangeRequired(true);
			user.setTempPlaintextPassword(tempPasswordPlaintext);

			userRepository.save(user);
			message = "Institute application has been accepted successfully. User account created and activated.";

		} else if (newStatus == InstituteStatus.PENDING) {
			institute.setRejectionReason(null);
			institute.setCorrectionPendingReview(false);
			message = "Institute application status has been set to pending.";
			userOpt.ifPresent(user -> {
				if (user.isEnabled()) {
					user.setEnabled(false);
					user.setPasswordChangeRequired(false);
					user.setTempPlaintextPassword(null);
					userRepository.save(user);
				}
			});
		}

		Institute savedInstitute = instituteRepository.save(institute);
		return new InstituteApprovalResponseDTO(savedInstitute, tempUsername, tempPasswordPlaintext, message);
	}

	@Override
	@Transactional
	public InstituteStatusResponseDTO checkInstituteStatus(InstituteStatusRequestDTO instituteRequestDTO) {
		InstituteStatusResponseDTO response = new InstituteStatusResponseDTO();

		Institute institute = null;
		institute = instituteRepository
				.findByAISHEIdIgnoreCaseAndInstitutionOfficialEmailIdIgnoreCaseAndInstitutionOfficialContactNumber(
						instituteRequestDTO.getIdentifier().trim().toUpperCase(),
						instituteRequestDTO.getEmail().trim().toLowerCase(), instituteRequestDTO.getMobile().trim())
				.orElse(null);

		if (institute == null) {
			response.setStatus("NOT_FOUND");
			response.setMessage("No institute found with the provided Email ID or AISHE ID. Please check your input.");
			response.setRequiresPasswordReset(false);
			return response;
		}

		response.setInstituteId(institute.getInstituteId());
		response.setInstituteName(institute.getInstituteName());
		response.setStatus(institute.getStatus().name());
		response.setRejectionReason(institute.getRejectionReason());
		response.setCorrectionPendingReview(institute.isCorrectionPendingReview());

		Optional<User> userOpt = userRepository.findByOrgOwnerTypeAndOrgOwnerId(OrgOwnerType.INSTITUTE,
				institute.getInstituteId());

		switch (institute.getStatus()) {
		case ACCEPTED:
			if (userOpt.isPresent()) {
				User user = userOpt.get();
				response.setUsername(user.getUsername());
				response.setRequiresPasswordReset(user.isPasswordChangeRequired());

				if (user.isPasswordChangeRequired() && user.getTempPlaintextPassword() != null) {
					response.setTemporaryPassword(user.getTempPlaintextPassword());
					response.setMessage(
							"Your institute registration has been approved! Use the provided username and temporary password below to log in. You will be immediately prompted to set your new password.");
				} else if (user.isPasswordChangeRequired() && user.getTempPlaintextPassword() == null) {
					response.setMessage(
							"Your institute registration has been approved! Please proceed to login with your username. If you haven't set your permanent password, please use the 'Forgot Password' link to set it.");
				} else {
					response.setMessage(
							"Your institute registration has been approved and you have already set your password. Please proceed to login.");
				}
			} else {
				response.setMessage(
						"Your institute registration has been approved, but no user account was found. Please contact support.");
				response.setRequiresPasswordReset(false);
			}
			break;
		case REJECTED:
			response.setMessage("Your institute registration has been rejected. Please review the rejection reason.");
			response.setRequiresPasswordReset(false);
			break;
		case CORRECTION_REQUIRED:
			response.setMessage(
					"Your institute registration requires corrections. Please review the feedback and resubmit your application.");
			response.setRequiresPasswordReset(false);
			break;
		case PENDING:
		default:
			if (response.isCorrectionPendingReview()) {
				response.setMessage(
						"Your corrections have been submitted successfully. Your application is now pending review of previously submitted corrections.");
			} else {
				response.setMessage(
						"Your institute registration is currently pending initial review. Please check again later.");
			}
			response.setRequiresPasswordReset(false);
			break;
		}

		return response;
	}

	@Override
	public List<Institute> getAllInstitutes() {
		return instituteRepository.findAll();
	}

	@Override
	public List<Institute> getInstitutesByStatus(String status) {
		try {
			InstituteStatus statusEnum = InstituteStatus.valueOf(status.toUpperCase());
			return instituteRepository.findByStatus(statusEnum);
		} catch (IllegalArgumentException e) {
			return List.of();
		}
	}

	@Override
	public List<Institute> getLatestInstitutes() {
		return instituteRepository.findTop5ByOrderByInstituteIdDesc();
	}

	@Override
	public Institute findById(Short id) {
		return instituteRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Institute not found with ID: " + id));
	}

	@Override
	public boolean isAisheIdUnique(String aisheId, Short currentInstituteId) {
		if (aisheId == null || aisheId.isBlank()) {
			return true;
		}
		String formattedAisheId = aisheId.trim().toUpperCase();
		if (currentInstituteId == null) {
			return !instituteRepository.existsByAISHEIdIgnoreCase(formattedAisheId);
		} else {
			return !instituteRepository.existsByAISHEIdIgnoreCaseAndInstituteIdNot(formattedAisheId,
					currentInstituteId);
		}
	}

	@Override
	public boolean isEmailUnique(String email, Short currentInstituteId) {
		if (email == null || email.isBlank()) {
			return true;
		}
		if (currentInstituteId == null) {
			return !instituteRepository.existsByInstitutionOfficialEmailIdIgnoreCase(email);
		} else {
			return !instituteRepository.existsByInstitutionOfficialEmailIdIgnoreCaseAndInstituteIdNot(email,
					currentInstituteId);
		}
	}

	@Override
	public boolean isContactNumberUnique(String contactNumber, Short currentInstituteId) {
		if (contactNumber == null || contactNumber.isBlank()) {
			return true;
		}
		if (currentInstituteId == null) {
			return !instituteRepository.existsByInstitutionOfficialContactNumber(contactNumber);
		} else {
			return !instituteRepository.existsByInstitutionOfficialContactNumberAndInstituteIdNot(contactNumber,
					currentInstituteId);
		}
	}

	@Override
	public boolean isWebsiteUnique(String website, Short currentInstituteId) {
		if (website == null || website.isBlank()) {
			return true;
		}
		if (currentInstituteId == null) {
			return !instituteRepository.existsByInstitutionWebsiteIgnoreCase(website);
		} else {
			return !instituteRepository.existsByInstitutionWebsiteIgnoreCaseAndInstituteIdNot(website,
					currentInstituteId);
		}
	}

	@Override
	public Short findInstituteIdByUsername(String username) {
		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new IllegalArgumentException("User not found with username: " + username));

		if (user.getOrgOwnerType() == OrgOwnerType.INSTITUTE && user.getOrgOwnerId() != null) {
			return user.getOrgOwnerId();
		} else {
			throw new IllegalArgumentException("User does not belong to an Institute: " + username);
		}
	}

	@Override
	public InstituteRequestDTO convertToRequestDTO(Institute institute) {
		InstituteRequestDTO dto = new InstituteRequestDTO();

		dto.setInstituteId(institute.getInstituteId());
		dto.setInstituteName(institute.getInstituteName());
		dto.setAISHEId(institute.getAISHEId());
		dto.setYearEstablished(institute.getYearEstablished());
		dto.setBorderDistrictArea(institute.getBorderDistrictArea());
		dto.setUniversityName(institute.getUniversityName());
		dto.setInstitutionHeadDetails(institute.getInstitutionHeadDetails());
		dto.setInstitutionOfficialEmailId(institute.getInstitutionOfficialEmailId());
		dto.setInstitutionOfficialContactNumber(institute.getInstitutionOfficialContactNumber());
		dto.setInstitutionWebsite(institute.getInstitutionWebsite());
		dto.setAffiliationTypeId(institute.getAffiliationType().getAffiliationTypeId());
		dto.setManagementTypeId(institute.getManagementType().getManagementTypeId());

		// --- THIS FIXES THE GHOST BUTTONS AND PENDING STATUS ---
		dto.setStatus(institute.getStatus() != null ? institute.getStatus() : InstituteStatus.PENDING);
		String rawUrl = institute.getProspectusUrl();
		dto.setProspectusUrl((rawUrl == null || rawUrl.trim().isEmpty()) ? null : rawUrl.trim());
		// -------------------------------------------------------

		Address address = institute.getAddress();
		AddressDTO addressDto = new AddressDTO();
		if (address != null) {
			addressDto.setAddressLine1(address.getAddressLine1());
			addressDto.setAddressLine2(address.getAddressLine2());
			addressDto.setPincode(address.getPincode());
			addressDto.setStateCode(address.getState() != null ? address.getState().getStateCode() : null);
			addressDto.setDistrictCode(address.getDistrict() != null ? address.getDistrict().getDistrictCode() : null);
			addressDto.setBlockCode(address.getBlock() != null ? address.getBlock().getBlockCode() : null);
		}
		dto.setAddressDTO(addressDto);

		return dto;
	}

	@Override
	@Transactional
	public void updateProspectusUrl(String username, String prospectusUrl) {
		Short instituteId = findInstituteIdByUsername(username);
		Institute institute = instituteRepository.findById(instituteId)
				.orElseThrow(() -> new IllegalArgumentException("Institute not found with ID: " + instituteId));

		institute.setProspectusUrl(prospectusUrl);
		instituteRepository.save(institute);
	}

	@Override
	public long getTotalInstituteCount() {
		return instituteRepository.count();
	}

	@Override
	public long getInstituteCountByStatus(String status) {
		try {
			InstituteStatus statusEnum = InstituteStatus.valueOf(status.toUpperCase());
			return instituteRepository.countByStatus(statusEnum);
		} catch (IllegalArgumentException e) {
			return 0;
		}
	}

	@Override
	@Transactional(readOnly = true)
	public Short getInstituteIdByUsername(String username) {
		return findInstituteIdByUsername(username);
	}

	@Override
	public InstituteStatusResponseDTO checkInstituteStatus(String identifier) {
		// TODO Auto-generated method stub
		return null;
	}

	private static String sanitizeForLog(String input) {
		if (input == null) {
			return "(null)";
		}
		// Strip CR/LF — the primary log-forging attack vectors
		String sanitized = input.replaceAll("[\r\n\t]", "_");
		// Truncate to prevent log flooding from oversized input
		if (sanitized.length() > 50) {
			sanitized = sanitized.substring(0, 50) + "...[truncated]";
		}
		return sanitized;
	}
}