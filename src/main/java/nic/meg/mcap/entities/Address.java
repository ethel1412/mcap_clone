package nic.meg.mcap.entities;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nic.meg.mcap.utils.StringCryptoConverter;

@Entity
@Getter
@Setter
@NoArgsConstructor

public class Address {

	@Id
	@GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
	@Column(nullable = false, length = 4)
	private Integer addressId;

	@Convert(converter = StringCryptoConverter.class)
	@Column(length = 1024)
	private String addressLine1;

	@Convert(converter = StringCryptoConverter.class)
	@Column(length = 1024)
	private String addressLine2;

	@Column(nullable = false, length = 6)
	private String pincode;

	@ManyToOne
	@JoinColumn(name = "stateCode")
	@JsonIgnore
	private State state;

	@ManyToOne
	@JoinColumn(name = "districtCode")
	@JsonIgnore
	private District district;

	@ManyToOne
	@JoinColumn(name = "block_code", nullable = true)
	@JsonIgnore
	private Block block;

	@Column(name = "town_village", length = 255)
	private String townVillage;

	@Column(nullable = false, length = 20)
	private String userType;

	@Column(nullable = false, length = 20)
	private String addressType;

	private UUID entityId;
}
