package nic.meg.mcap.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdmissionWindowProgramme {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Short id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "admission_id", nullable = false)
	private AdmissionWindow admissionWindow;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "programme_id", nullable = false)
	private Programme programme;

	@Column(nullable = false, columnDefinition = "boolean default true")
    private boolean isActive = true;

}
