
package nic.meg.mcap.services.impl;

import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import nic.meg.mcap.dto.request.ApplicantAddressRequestDTO;
import nic.meg.mcap.dto.request.ApplicantDTO;
import nic.meg.mcap.dto.request.PersonalDetailsRequestDTO;
import nic.meg.mcap.dto.request.RegistrationFormDTO;
import nic.meg.mcap.entities.Address;
import nic.meg.mcap.entities.AdmissionWindow;
import nic.meg.mcap.entities.Applicant;
import nic.meg.mcap.entities.Application;
import nic.meg.mcap.entities.District;
import nic.meg.mcap.entities.Gender;
import nic.meg.mcap.entities.Role;
import nic.meg.mcap.entities.SequenceGenerator;
import nic.meg.mcap.entities.State;
import nic.meg.mcap.entities.User;
import nic.meg.mcap.enums.ApplicantType;
import nic.meg.mcap.exception.ApplicationAlreadyExistsException;
import nic.meg.mcap.repositories.AddressRepository;
import nic.meg.mcap.repositories.AdmissionWindowRepository;
import nic.meg.mcap.repositories.ApplicantRepository;
import nic.meg.mcap.repositories.ApplicationRepository;
import nic.meg.mcap.repositories.CommunityCategoryRepository;
import nic.meg.mcap.repositories.CountryRepository;
import nic.meg.mcap.repositories.CuetScoreRepository;
import nic.meg.mcap.repositories.DistrictRepository;
import nic.meg.mcap.repositories.GenderRepository;
import nic.meg.mcap.repositories.JeeScoreRepository;
import nic.meg.mcap.repositories.MaritalStatusRepository;
import nic.meg.mcap.repositories.RelationshipRepository;
import nic.meg.mcap.repositories.ReligionRepository;
import nic.meg.mcap.repositories.RoleRepository;
import nic.meg.mcap.repositories.SequenceGeneratorRepository;
import nic.meg.mcap.repositories.StateRepository;
import nic.meg.mcap.repositories.UserRepository;
import nic.meg.mcap.services.ApplicantService;
import nic.meg.mcap.utils.ApplicationCodeGenerator;
import nic.meg.mcap.utils.RSAUtil;

@Service
@Transactional
public class ApplicantServiceImpl implements ApplicantService {
	private static final Logger logger = LoggerFactory.getLogger(ApplicantServiceImpl.class);
	@Autowired
	private ModelMapper modelMapper;
	@Autowired
	private ApplicantRepository applicantRepository;
	@Autowired
	private RoleRepository roleRepository;
	@Autowired
	private AddressRepository addressRepository;
	@Autowired
	private StateRepository stateRepository;
	@Autowired
	private DistrictRepository districtRepository;
	@Autowired
	private ReligionRepository religionRepository;
	@Autowired
	private MaritalStatusRepository maritalStatusRepository;
	@Autowired
	private CountryRepository countryRepository;
	@Autowired
	private CommunityCategoryRepository communityCategoryRepository;
	@Autowired
	private GenderRepository genderRepository;
	@Autowired
	private RelationshipRepository relationshipRepository;
	@Autowired
	private AdmissionWindowRepository admissionWindowRepository;
	@Autowired
	private ApplicationRepository applicationRepository;
	@Autowired
	private SequenceGeneratorRepository sequenceGeneratorRepository;
	@Autowired
	private JeeScoreRepository jeeScoreRepository;
	@Autowired
	private CuetScoreRepository cuetScoreRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private RSAUtil rsaUtil;

	@Override
	@Transactional
	public ApplicantDTO registerApplicant(RegistrationFormDTO formDTO, Short admissionId) {

		LocalDateTime now = LocalDateTime.now();
		Applicant applicant = new Applicant();
		User user = new User();
		boolean isNewUser = true;
		Application firstApplication = new Application();

		AdmissionWindow window = admissionWindowRepository
				.findByAdmissionIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(admissionId, now, now)
				.orElseThrow(() -> new RuntimeException("No active admission window found"));

		int existingCount = applicantRepository.countByPhoneNumber(formDTO.getPhoneNumber());

		if (existingCount > 3) {
			throw new ApplicationAlreadyExistsException("Maximum 3 applications allowed per phone number");
		}

		modelMapper.map(formDTO, applicant);

		try {
			String decryptedDob = rsaUtil.decrypt(formDTO.getDateOfBirth());

			if (decryptedDob == null || decryptedDob.isBlank()) {
				throw new IllegalArgumentException("Invalid Date of Birth");
			}

			LocalDate dob = LocalDate.parse(decryptedDob);
			applicant.setDateOfBirth(dob);

		} catch (DateTimeParseException e) {
			throw new IllegalArgumentException("Invalid Date format for Date of Birth", e);

		} catch (GeneralSecurityException e) {
			throw new IllegalArgumentException("Error decrypting Date of Birth", e);
		}

		Gender gender = Gender.builder().genderCode(formDTO.getGenderCode()).build();
		applicant.setGender(gender);
		firstApplication.setApplicant(applicant);
		firstApplication.setAdmissionWindow(window);
		Integer applicationSequence = getNextApplicationSequenceForWindow(window);
		String applicationNo = ApplicationCodeGenerator.generate(window.getAdmissionId(), applicationSequence);
		firstApplication.setApplicationNo(applicationNo);
		applicant.setApplicantNo(applicationNo);
		firstApplication.setApplicantType(ApplicantType.UNKNOWN);
		applicant.getApplications().add(firstApplication);

		user.setUsername(formDTO.getPhoneNumber());

		try {
			String decryptedDob = rsaUtil.decrypt(formDTO.getDateOfBirth());

			if (decryptedDob == null || decryptedDob.isBlank()) {
				throw new IllegalArgumentException("Invalid Date of Birth");
			}

			LocalDate dob = LocalDate.parse(decryptedDob);
			applicant.setDateOfBirth(dob);

		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid or corrupted Date of Birth");
		}

		try {
			String decryptedPassword = rsaUtil.decrypt(formDTO.getPassword());
			if (decryptedPassword == null || decryptedPassword.isBlank()) {
				throw new IllegalArgumentException("Invalid Password");
			}

			String hashedPassword = passwordEncoder.encode(decryptedPassword);
			user.setPassword(hashedPassword);

		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid or corrupted Password");
		}

		user.setOrgOwnerType(nic.meg.mcap.enums.OrgOwnerType.APPLICANT);
		Role applicantRole = roleRepository.findById("5").orElseThrow(() -> new RuntimeException("Role not found"));
		applicantRole.setRoleId(applicantRole.getRoleId());
		user.setRole(applicantRole);
		user.setEnabled(true);
		user.setAccountNonExpired(true);
		user.setAccountNonLocked(true);
		user.setCredentialsNonExpired(true);
		user.setPhoneNumber(null);
		userRepository.save(user);
		applicant.setUser(user);

		// check duplicates
		boolean duplicateByEmail = applicantRepository.existsByFirstNameIgnoreCaseAndDateOfBirthAndEmailIgnoreCase(
				applicant.getFirstName(), applicant.getDateOfBirth(), applicant.getEmail());

		boolean duplicateByPhone = applicantRepository.existsByFirstNameIgnoreCaseAndDateOfBirthAndPhoneNumber(
				applicant.getFirstName(), applicant.getDateOfBirth(), applicant.getPhoneNumber());

		boolean duplicateEmail = applicantRepository.existsByEmailIgnoreCaseTrimmed(applicant.getEmail());

		if (duplicateByEmail || duplicateByPhone) {

			throw new RuntimeException("Duplicate applicant found with same First Name + DOB + Email/Phone Number");
		}

		if (duplicateEmail) {

			throw new RuntimeException("Duplicate applicant with same email");
		}
		applicant = applicantRepository.save(applicant);

		ApplicantDTO returnDto = convertToDto(applicant);
		returnDto.setNewUser(isNewUser);
		return returnDto;
	}

	@Override
	@Transactional
	public void updatePersonalDetails(String applicantNo, PersonalDetailsRequestDTO dto) {
		Applicant applicant = applicantRepository.findByApplicantNo(applicantNo)
				.orElseThrow(() -> new RuntimeException("Applicant not found to update: " + applicantNo));

		modelMapper.map(dto, applicant);

		if (dto.getGenderCode() != null && !dto.getGenderCode().isEmpty()) {
			applicant.setGender(genderRepository.findById(dto.getGenderCode()).orElse(null));
		} else {
			applicant.setGender(null);
		}

		if (dto.getCommunityCategoryCode() != null && !dto.getCommunityCategoryCode().isEmpty()) {
			applicant.setCommunityCategory(
					communityCategoryRepository.findById(dto.getCommunityCategoryCode()).orElse(null));
		} else {
			applicant.setCommunityCategory(null);
		}

		if (dto.getMaritalStatusCode() != null) {
			applicant.setMaritalStatus(maritalStatusRepository.findById(dto.getMaritalStatusCode()).orElse(null));
		} else {
			applicant.setMaritalStatus(null);
		}

		if (dto.getGuardianRelationshipCode() != null) {
			applicant.setGuardianRelationship(
					relationshipRepository.findById(dto.getGuardianRelationshipCode()).orElse(null));
		} else {
			applicant.setGuardianRelationship(null);
		}

		if (dto.getReligionCode() != null) {
			applicant.setReligion(religionRepository.findById(dto.getReligionCode()).orElse(null));
		} else {
			applicant.setReligion(null);
		}

		if (dto.getCountryCode() != null) {
			applicant.setCountryOfOrigin(countryRepository.findById(dto.getCountryCode()).orElse(null));
		} else {
			applicant.setCountryOfOrigin(null);
		}
		if (dto.getPhoneNumber() != null && !dto.getPhoneNumber().isBlank()) {
			User user = applicant.getUser();
			if (user != null) {
				user.setUsername(dto.getPhoneNumber());
				userRepository.save(user);
			}
		}
		applicant.setHasDomicileCertificate(dto.getHasDomicileCertificate());
		applicant.setIsDifferentlyAbled(dto.getIsDifferentlyAbled());
		applicant.setHasNccCertificate(dto.getHasNccCertificate());
		applicant.setHasNssCertificate(dto.getHasNssCertificate());
		applicant.setHasBackwardAreaCertificate(dto.getHasBackwardAreaCertificate());
		applicant.setHasAnyOtherRelevantCertificate(dto.getHasAnyOtherRelevantCertificate());

		updateApplicantAddress(applicant.getApplicantId(), "Permanent", dto.getPermanentAddress());
		updateApplicantAddress(applicant.getApplicantId(), "Communication", dto.getCommunicationAddress());

		applicantRepository.save(applicant);
	}

	private void updateApplicantAddress(UUID applicantId, String addressType, ApplicantAddressRequestDTO addressDto) {
		if (addressDto == null) {
			return;
		}

		Address address = addressRepository.findByEntityIdAndAddressType(applicantId, addressType).orElseGet(() -> {
			Address newAddress = new Address();
			newAddress.setEntityId(applicantId);
			newAddress.setAddressType(addressType);
			newAddress.setUserType("Applicant");
			return newAddress;
		});

		address.setAddressLine1(addressDto.getAddressLine1());
		address.setAddressLine2(addressDto.getAddressLine2());
		address.setPincode(addressDto.getPincode());

		State state = (addressDto.getStateCode() != null)
				? stateRepository.findById(addressDto.getStateCode()).orElse(null)
				: null;
		District district = (addressDto.getDistrictCode() != null)
				? districtRepository.findById(addressDto.getDistrictCode()).orElse(null)
				: null;

		address.setState(state);
		address.setDistrict(district);

		address.setTownVillage(addressDto.getTownVillage());
		address.setBlock(null);

		addressRepository.save(address);
	}

	private boolean hasEntranceScore(Applicant applicant) {
		return jeeScoreRepository.findByApplicant(applicant).isPresent()
				|| cuetScoreRepository.findByApplicant(applicant).isPresent();
	}

	@Override
	public Page<ApplicantDTO> getAllApplicants(Pageable pageable) {
		return applicantRepository.findAll(pageable).map(this::convertToDto);
	}

	private ApplicantDTO convertToDto(Applicant applicant) {
		return modelMapper.map(applicant, ApplicantDTO.class);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Integer getNextApplicationSequenceForWindow(AdmissionWindow window) {

		Short admissionId = window.getAdmissionId();

		String sequenceName = "WINDOW_APP_" + admissionId;

		SequenceGenerator sequence = sequenceGeneratorRepository.findById(sequenceName).orElseGet(() -> {
			SequenceGenerator newSeq = new SequenceGenerator();
			newSeq.setSequenceName(sequenceName);
			newSeq.setAdmissionWindow(window);
			newSeq.setNextValue(1L);
			return sequenceGeneratorRepository.save(newSeq);
		});

		Integer next = (int) sequence.getNextValue();
		sequence.setNextValue(next + 1);

		return next;
	}

	@Override
	@Transactional(readOnly = true)
	public PersonalDetailsRequestDTO getPersonalDetailsForForm(String applicantNo) {
		Applicant applicant = applicantRepository.findByApplicantNo(applicantNo)
				.orElseThrow(() -> new RuntimeException("Applicant not found: " + applicantNo));

		List<Address> addresses = addressRepository.findByEntityId(applicant.getApplicantId());
		Address permAddress = addresses.stream().filter(a -> "Permanent".equalsIgnoreCase(a.getAddressType()))
				.findFirst().orElse(new Address());
		Address commAddress = addresses.stream().filter(a -> "Communication".equalsIgnoreCase(a.getAddressType()))
				.findFirst().orElse(new Address());

		PersonalDetailsRequestDTO dto = modelMapper.map(applicant, PersonalDetailsRequestDTO.class);
		if (applicant.getMaritalStatus() != null) {
			dto.setMaritalStatusCode(applicant.getMaritalStatus().getStatusCode().trim());
		}
		if (applicant.getCommunityCategory() != null) {
			dto.setCommunityCategoryCode(applicant.getCommunityCategory().getCategoryCode().trim());
		}
		if (applicant.getReligion() != null) {
			dto.setReligionCode(applicant.getReligion().getReligionCode().trim());
		}
		if (applicant.getCountryOfOrigin() != null) {
			dto.setCountryCode(applicant.getCountryOfOrigin().getCountryCode());
		}
		dto.setGuardianName(applicant.getGuardianName());
		if (applicant.getGuardianRelationship() != null) {
			dto.setGuardianRelationshipCode(applicant.getGuardianRelationship().getRelationshipCode());
		}

		ApplicantAddressRequestDTO permAddressDto = new ApplicantAddressRequestDTO();
		permAddressDto.setAddressLine1(permAddress.getAddressLine1());
		permAddressDto.setAddressLine2(permAddress.getAddressLine2());
		permAddressDto.setPincode(permAddress.getPincode());
		if (permAddress.getState() != null) {
			permAddressDto.setStateCode(permAddress.getState().getStateCode());
		}
		if (permAddress.getDistrict() != null) {
			permAddressDto.setDistrictCode(permAddress.getDistrict().getDistrictCode());
		}
		permAddressDto.setTownVillage(permAddressDto.getTownVillage());
		dto.setPermanentAddress(permAddressDto);

		ApplicantAddressRequestDTO commAddressDto = new ApplicantAddressRequestDTO();
		commAddressDto.setAddressLine1(commAddress.getAddressLine1());
		commAddressDto.setAddressLine2(commAddress.getAddressLine2());
		commAddressDto.setPincode(commAddress.getPincode());
		if (commAddress.getState() != null) {
			commAddressDto.setStateCode(commAddress.getState().getStateCode());
		}
		if (commAddress.getDistrict() != null) {
			commAddressDto.setDistrictCode(commAddress.getDistrict().getDistrictCode());
		}
		commAddressDto.setTownVillage(commAddressDto.getTownVillage());
		dto.setCommunicationAddress(commAddressDto);

		return dto;
	}

	@Override
	@Transactional
	public void finalizeDocuments(Long applicationId, String applicantNo) {
		Application application = applicationRepository.findById(applicationId)
				.orElseThrow(() -> new EntityNotFoundException("Application not found."));

		if (!application.getApplicant().getApplicantNo().equals(applicantNo)) {
			throw new SecurityException("You do not have permission to modify this application.");
		}

		application.setDocumentsFinalized(true);
		applicationRepository.save(application);
	}

	@Override
	@Transactional(readOnly = true)
	public ApplicantAddressRequestDTO getApplicantAddress(UUID applicantId, String addressType) {
		Address address = addressRepository.findByEntityIdAndAddressType(applicantId, addressType)
				.orElse(new Address()); // Find address or create an empty one

		// Create the correct DTO type
		ApplicantAddressRequestDTO dto = new ApplicantAddressRequestDTO();
		dto.setAddressLine1(address.getAddressLine1());
		dto.setAddressLine2(address.getAddressLine2());
		dto.setPincode(address.getPincode());
		dto.setTownVillage(address.getTownVillage());

		if (address.getState() != null) {
			dto.setStateCode(address.getState().getStateCode());
		}
		if (address.getDistrict() != null) {
			dto.setDistrictCode(address.getDistrict().getDistrictCode());
		}

		return dto;
	}

	@Override
	@Transactional
	public void setLastSelectedApplication(String applicantNo, Long applicationId) {
		Applicant applicant = applicantRepository.findByApplicantNo(applicantNo)
				.orElseThrow(() -> new EntityNotFoundException("Applicant not found: " + applicantNo));

		boolean applicationBelongsToApplicant = applicant.getApplications().stream()
				.anyMatch(app -> app.getApplicationId().equals(applicationId));

		if (!applicationBelongsToApplicant) {
			throw new SecurityException("Applicant " + applicantNo + " does not own application " + applicationId);
		}

		applicant.setLastSelectedApplicationId(applicationId);
		applicantRepository.save(applicant);
	}

	@Override
	@Transactional
	public void finalizeApplication(Long applicationId, String applicantNo) {
		Application application = applicationRepository.findById(applicationId)
				.orElseThrow(() -> new EntityNotFoundException("Application not found."));

		if (!application.getApplicant().getApplicantNo().equals(applicantNo)) {
			throw new SecurityException("You do not have permission to modify this application.");
		}

		Applicant applicant = application.getApplicant();

		boolean hasEntrance = hasEntranceScore(applicant);

		application.setApplicantType(hasEntrance ? ApplicantType.WITH_ENTRANCE : ApplicantType.WITHOUT_ENTRANCE);

		application.setPaymentComplete(true);
		application.setApplicationStatus("COMPLETE");

		applicationRepository.save(application);
	}

}
