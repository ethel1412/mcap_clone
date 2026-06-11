package nic.meg.mcap.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity

@Getter
@Setter
@NoArgsConstructor

public class District {
	@Id
	private short districtCode;

	@Column(length = 50)
	private String districtName;

	@ManyToOne
	@JoinColumn(name = "state_code", nullable = false)
	private State state;
}
