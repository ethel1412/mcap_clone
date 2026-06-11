package nic.meg.mcap.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nic.meg.mcap.audit.AuditTable;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class UserActivitiesResponse {

	private String username;
	private String ipAddress;
	private Boolean isSuccess;
	private String time;
	private List<AuditTable> auditTable;
}