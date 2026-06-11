package nic.meg.mcap.entities;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nic.meg.mcap.audit.AuditingEntityListener;
import nic.meg.mcap.enums.OrgOwnerType;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class User implements UserDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer userId;

	private UUID userCode;

	@NotNull
	@Pattern(regexp = "^[a-zA-Z0-9\\-]{3,50}$", message = "Username must be 3-50 characters. Only letters, numbers, and hyphens allowed.")
	private String username;

	@NotNull
	@Size(max = 250, message = "Password should have maximum 250 characters")
	private String password;

	@Column
	private String tempPlaintextPassword; // Will store the plaintext temporary password for one-time display

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private OrgOwnerType orgOwnerType;

	private Short orgOwnerId;

	private Boolean isSuperuser;
	private Boolean enabled;
	private Boolean accountNonExpired;
	private Boolean accountNonLocked;
	private Boolean credentialsNonExpired;

	@Column(nullable = false)
	private boolean passwordChangeRequired = false;

	@CreationTimestamp
	private Timestamp dateJoined;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "roleId", referencedColumnName = "roleId", nullable = false)
	private Role role;

	@Pattern(regexp = "^\\d{10}$", message = "Phone number must be 10 digits")
	@Column(length = 10, unique = true)
	private String phoneNumber;

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority("ROLE_" + role.getRoleName().toUpperCase()));
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public boolean isAccountNonExpired() {
		return accountNonExpired;
	}

	@Override
	public boolean isAccountNonLocked() {
		return accountNonLocked;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return credentialsNonExpired;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

}