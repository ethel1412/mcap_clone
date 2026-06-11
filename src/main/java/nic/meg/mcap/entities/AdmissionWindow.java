package nic.meg.mcap.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import nic.meg.mcap.enums.ProgrammeLevel;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AdmissionWindow {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private short admissionId;

	@Column(unique = true, nullable = false, length = 30)
	private String admissionCode;

	@ManyToOne
	@JoinColumn(name = "stream_id", nullable = true)
	private Stream stream;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private ProgrammeLevel programmeLevel;

	@Column(nullable = false, length = 20)
	private String session;

	// Active Application Period
	@Column(nullable = false)
	private LocalDateTime startDate;

	@Column(nullable = false)
	private LocalDateTime endDate;

	@Column(nullable = false)
	private boolean isExtended = false;

	private LocalDateTime originalEndDate;

	@Column(nullable = false)
	private boolean isActive;

	@OneToMany(mappedBy = "admissionWindow", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<AdmissionWindowProgramme> admissionWindowProgrammes = new ArrayList<>();

	// Sequential steps (Pre-Admission before window, Counselling after window)
	@OneToMany(mappedBy = "admissionWindow", cascade = CascadeType.ALL)
	private List<Schedule> schedules = new ArrayList<>();
}