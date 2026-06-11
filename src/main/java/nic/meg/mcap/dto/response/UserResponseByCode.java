package nic.meg.mcap.dto.response;

import java.sql.Timestamp;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nic.meg.mcap.entities.Role;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class UserResponseByCode {
	private UUID userCode;
	private String username;
	private Boolean accountNonExpired;
	private Boolean accountNonLocked;
	private Boolean enabled;
	private Timestamp dateJoined;
	private RoleDTO role;
}