package nic.meg.mcap.services;

import java.util.List;

import nic.meg.mcap.dto.request.InstituteRequestDTO;
import nic.meg.mcap.dto.request.InstituteStatusRequestDTO;
import nic.meg.mcap.dto.response.InstituteApprovalResponseDTO;
import nic.meg.mcap.dto.response.InstituteResponseDTO;
import nic.meg.mcap.dto.response.InstituteStatusResponseDTO;
import nic.meg.mcap.entities.Institute;

public interface InstituteService {
	// Modified to return InstituteResponseDTO (which no longer has credentials)
	InstituteResponseDTO saveInstitute(InstituteRequestDTO dto);

	InstituteApprovalResponseDTO updateStatus(Short instituteId, String status, String reason);

	List<Institute> getAllInstitutes();

	List<Institute> getInstitutesByStatus(String status);

	List<Institute> getLatestInstitutes();

	Institute findById(Short id);

	InstituteStatusResponseDTO checkInstituteStatus(String identifier);

	// It include currentInstituteId for uniqueness checks
	boolean isAisheIdUnique(String aisheId, Short currentInstituteId);

	boolean isEmailUnique(String email, Short currentInstituteId);

	boolean isContactNumberUnique(String contactNumber, Short currentInstituteId);

	boolean isWebsiteUnique(String website, Short currentInstituteId);

	Short findInstituteIdByUsername(String username);

	InstituteRequestDTO convertToRequestDTO(Institute institute);

	void updateProspectusUrl(String username, String prospectusUrl);

	long getTotalInstituteCount();

	long getInstituteCountByStatus(String status);

	Short getInstituteIdByUsername(String username);

	InstituteStatusResponseDTO checkInstituteStatus(InstituteStatusRequestDTO instituteRequestDTO);

}