package nic.meg.mcap.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import nic.meg.mcap.audit.AuditingEntityListener;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString

public class Country {
	@Id
	private short countryCode;

	@Column(nullable = false, length = 50)
	private String countryName;

	@Column(length = 10)
	private String phoneCode;
}