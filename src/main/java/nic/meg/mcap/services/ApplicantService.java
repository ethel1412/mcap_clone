package nic.meg.mcap.services;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import nic.meg.mcap.dto.request.ApplicantAddressRequestDTO;
import nic.meg.mcap.dto.request.ApplicantDTO;
import nic.meg.mcap.dto.request.PersonalDetailsRequestDTO;
import nic.meg.mcap.dto.request.RegistrationFormDTO;

public interface ApplicantService {
	ApplicantDTO registerApplicant(RegistrationFormDTO formDTO, Short admissionId);

//	Application createApplicationForExistingUser(String applicantNo, Short admissionId);

	Page<ApplicantDTO> getAllApplicants(Pageable pageable);

	PersonalDetailsRequestDTO getPersonalDetailsForForm(String applicantNo);

	void updatePersonalDetails(String applicantNo, PersonalDetailsRequestDTO dto);

	void finalizeDocuments(Long applicationId, String applicantNo);

	void finalizeApplication(Long applicationId, String applicantNo);

	ApplicantAddressRequestDTO getApplicantAddress(UUID applicantId, String addressType);

	void setLastSelectedApplication(String applicantNo, Long applicationId);
}
