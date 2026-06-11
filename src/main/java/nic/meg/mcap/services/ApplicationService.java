package nic.meg.mcap.services;

import nic.meg.mcap.dto.response.ApplicationStatusResponseDTO;

public interface ApplicationService {
	ApplicationStatusResponseDTO updatePersonalDetailsStatus(Long applicationId, String applicantNo);

	ApplicationStatusResponseDTO updateProgrammeSelectionStatus(Long applicationId, String applicantNo);

	ApplicationStatusResponseDTO updateDocumentsUploadStatus(Long applicationId, String applicantNo);

	ApplicationStatusResponseDTO getApplicationStatus(Long applicationId, String applicantNo);

	ApplicationStatusResponseDTO confirmPayment(Long applicationId, String applicantNo);

	ApplicationStatusResponseDTO updateAcademicDetailsStatus(Long long1, String applicantNo);
}