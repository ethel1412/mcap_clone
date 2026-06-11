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
public class Block {

	@Id
	private short blockCode;

	@Column(nullable = false, length = 50)
	private String blockName;

	@ManyToOne
	@JoinColumn(name = "district_code", nullable = false)
	private District district;

}
