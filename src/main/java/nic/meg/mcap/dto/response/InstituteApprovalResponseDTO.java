package nic.meg.mcap.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nic.meg.mcap.entities.Institute;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InstituteApprovalResponseDTO {
	private Institute institute;
	private String username;
	private String temporaryPassword; // Only set if approved
	private String message;
}