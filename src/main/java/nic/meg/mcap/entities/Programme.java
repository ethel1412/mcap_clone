package nic.meg.mcap.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nic.meg.mcap.enums.ProgrammeLevel;
import nic.meg.mcap.enums.ProgrammeLevel;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Programme {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Short programmeId;

	@Column(nullable = false, length = 100)
	private String programmeName;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private ProgrammeLevel programmeLevel;

	@ManyToOne
	@JoinColumn(name = "stream_id", nullable = false)
	private Stream stream;
	
	@ManyToOne
	@JoinColumn(name = "department_id", nullable = false)
	private Department department;

}