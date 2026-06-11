package nic.meg.mcap.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nic.meg.mcap.enums.OrgOwnerType;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class UserDTO {

	private UUID userCode;

	@NotNull
//	@Pattern(regexp = "^[a-zA-Z0-9]{6,20}$", message = "Minimum 6 characters and maximum 20 characters. Special characters are not allowed for username.")
	private String username;

	@NotNull
	private String password;

	private OrgOwnerType orgOwnerType;

	private Short orgOwnerId;

	private Boolean isSuperuser;
	private Boolean enabled;
	private Boolean accountNonExpired;
	private Boolean accountNonLocked;
	private Boolean credentialsNonExpired;
	
	private String roleName;

}