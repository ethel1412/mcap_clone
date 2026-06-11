package nic.meg.mcap.dto.request;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import nic.meg.mcap.enums.ProgrammeLevel;

@Getter
@Setter
public class AdmissionWindowRequestDTO {

	private String admissionCode;

	@NotBlank(message = "You must select the scope of the window.")
	private String windowType;

	private Short streamId;

	@NotNull(message = "Programme level is required.")
	private ProgrammeLevel programmeLevel;

	@NotBlank(message = "Session is required.")
	private String session;

	@NotNull(message = "Start date is required.")
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	private LocalDateTime startDate;

	@NotNull(message = "End date is required.")
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	private LocalDateTime endDate;

	// --- NEW FIELDS FOR EXTENSION ---
	private boolean isExtended;
	private LocalDateTime originalEndDate;

	private List<Short> programmeIds;
}