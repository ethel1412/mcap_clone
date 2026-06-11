package nic.meg.mcap.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class SequenceGenerator {
	@Id
	private String sequenceName;
	@OneToOne
	@JoinColumn(name = "admission_id", nullable = false, unique = true)
	private AdmissionWindow admissionWindow;
	private long nextValue;
}