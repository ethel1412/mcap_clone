package nic.meg.mcap.services.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import nic.meg.mcap.dto.response.ApplicationStatusResponseDTO;
import nic.meg.mcap.entities.Application;
import nic.meg.mcap.repositories.ApplicationRepository;
import nic.meg.mcap.services.ApplicationService;

@Service
@Transactional
@RequiredArgsConstructor // Automatically creates a constructor for 'final' fields
public class ApplicationServiceImpl implements ApplicationService {

	// Removed @Autowired and added 'final' - this is the cleanest Spring Boot
	// standard
	private final ApplicationRepository applicationRepository;

	private Application findAndVerifyApplication(Long applicationId, String applicantNo) {
		Application application = applicationRepository.findById(applicationId)
				.orElseThrow(() -> new EntityNotFoundException("Application not found with ID: " + applicationId));
		if (!application.getApplicant().getApplicantNo().equals(applicantNo)) {
			throw new SecurityException("Unauthorized access to application " + applicationId);
		}
		return application;
	}

	@Override
	public ApplicationStatusResponseDTO updatePersonalDetailsStatus(Long applicationId, String applicantNo) {
		Application app = findAndVerifyApplication(applicationId, applicantNo);
		app.setPersonalDetailsComplete(true);
		Application savedApp = applicationRepository.save(app);
		return ApplicationStatusResponseDTO.fromEntity(savedApp);
	}

	@Override
	public ApplicationStatusResponseDTO updateAcademicDetailsStatus(Long applicationId, String applicantNo) {
		Application app = findAndVerifyApplication(applicationId, applicantNo);
		app.setAcademicDetailsComplete(true);
		Application savedApp = applicationRepository.save(app);
		return ApplicationStatusResponseDTO.fromEntity(savedApp);
	}

	@Override
	public ApplicationStatusResponseDTO updateProgrammeSelectionStatus(Long applicationId, String applicantNo) {
		Application app = findAndVerifyApplication(applicationId, applicantNo);
		app.setProgrammeSelectionComplete(true);
		Application savedApp = applicationRepository.save(app);
		return ApplicationStatusResponseDTO.fromEntity(savedApp);
	}

	@Override
	public ApplicationStatusResponseDTO updateDocumentsUploadStatus(Long applicationId, String applicantNo) {
		Application app = findAndVerifyApplication(applicationId, applicantNo);
		app.setDocumentsFinalized(true);
		Application savedApp = applicationRepository.save(app);
		return ApplicationStatusResponseDTO.fromEntity(savedApp);
	}

	@Override
	@Transactional(readOnly = true)
	public ApplicationStatusResponseDTO getApplicationStatus(Long applicationId, String applicantNo) {
		Application app = findAndVerifyApplication(applicationId, applicantNo);
		return ApplicationStatusResponseDTO.fromEntity(app);
	}

	@Override
	public ApplicationStatusResponseDTO confirmPayment(Long applicationId, String applicantNo) {
		// Cleaned up: Reused your secure helper method!
		Application application = findAndVerifyApplication(applicationId, applicantNo);

		application.setPaymentComplete(true);
		application.setApplicationStatus("SUBMITTED");
		application.setPaymentTimestamp(java.time.LocalDateTime.now());

		Application savedApplication = applicationRepository.save(application);

		return ApplicationStatusResponseDTO.fromEntity(savedApplication);
	}
}