package nic.meg.mcap.services.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import nic.meg.mcap.config.NotificationConfig;
import nic.meg.mcap.dto.request.ScheduleRequestDTO;
import nic.meg.mcap.dto.response.NextScheduleStepDTO;
import nic.meg.mcap.dto.response.ScheduleNotificationResponseDTO;
import nic.meg.mcap.dto.response.ScheduleResponseDTO;
import nic.meg.mcap.entities.AdmissionWindow;
import nic.meg.mcap.entities.Schedule;
import nic.meg.mcap.entities.ScheduleStepTemplate;
import nic.meg.mcap.enums.ScheduleActorRole;
import nic.meg.mcap.repositories.AdmissionWindowRepository;
import nic.meg.mcap.repositories.ScheduleRepository;
import nic.meg.mcap.repositories.ScheduleStepTemplateRepository;
import nic.meg.mcap.services.ScheduleService;

@Service
@Transactional
public class ScheduleServiceImpl implements ScheduleService {

	@Autowired
	private ScheduleRepository scheduleRepository;

	@Autowired
	private AdmissionWindowRepository admissionWindowRepository;

	@Autowired
	private ScheduleStepTemplateRepository templateRepository;

	@Autowired
	private NotificationConfig notificationConfig;

	@Override
	public List<ScheduleNotificationResponseDTO> getInstituteScheduleNotifications() {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime next30Days = now.plusDays(30);

		List<Schedule> instituteSchedules =
				scheduleRepository.findInstituteScheduleNotifications(
					    now,
					    next30Days,
					    ScheduleActorRole.INSTITUTE   // ✅ correct
					);
		List<ScheduleNotificationResponseDTO> notifications = new ArrayList<>();
		int urgentThresholdDays = notificationConfig.getUrgent().getThreshold();

		for (Schedule schedule : instituteSchedules) {
			ScheduleNotificationResponseDTO dto = new ScheduleNotificationResponseDTO();
			dto.setStepName(schedule.getStepName());
			dto.setDescription(schedule.getDescription());
			dto.setStartDate(schedule.getStartDate());
			dto.setEndDate(schedule.getEndDate());

			LocalDate today = LocalDate.now();
			LocalDate endDate = schedule.getEndDate().toLocalDate();
			long daysRemaining = ChronoUnit.DAYS.between(today, endDate);
			dto.setDaysRemaining((int) daysRemaining);

			if (now.isBefore(schedule.getStartDate())) {
				dto.setStatus("UPCOMING");
			} else if (now.isAfter(schedule.getEndDate())) {
				continue;
			} else if (daysRemaining <= urgentThresholdDays) {
				dto.setStatus("ENDING_SOON");
			} else {
				dto.setStatus("ACTIVE");
			}
			notifications.add(dto);
		}

		notifications.sort((a, b) -> {
			if ("ENDING_SOON".equals(a.getStatus()) && !"ENDING_SOON".equals(b.getStatus()))
				return -1;
			if (!"ENDING_SOON".equals(a.getStatus()) && "ENDING_SOON".equals(b.getStatus()))
				return 1;
			return Integer.compare(a.getDaysRemaining(), b.getDaysRemaining());
		});

		return notifications;
	}

	@Override
	public List<ScheduleResponseDTO> getSchedulesByAdmissionWindow(Short admissionId) {
		List<Schedule> schedules = scheduleRepository.findByAdmissionWindowIdOrderByStepOrder(admissionId);
		return schedules.stream().map(this::convertToDTO).collect(Collectors.toList());
	}

	@Override
	public List<ScheduleResponseDTO> getSchedulesByWindowAndCategory(Short admissionId, String category) {
		return scheduleRepository.findByAdmissionWindowIdAndCategoryOrderByStepOrder(admissionId, category).stream()
				.map(this::convertToDTO).collect(Collectors.toList());
	}

	@Override
	public NextScheduleStepDTO getNextAvailableStep(Short admissionId) {
		Optional<Integer> maxStepOrder = scheduleRepository.findMaxStepOrderByAdmissionWindow(admissionId);
		int nextOrder = maxStepOrder.orElse(0) + 1;

		Long totalStepsLong = templateRepository.countActiveTemplates();
		int totalSteps = totalStepsLong.intValue();
		int completedSteps = maxStepOrder.orElse(0);

		if (nextOrder > totalSteps) {
			return new NextScheduleStepDTO(null, null, true, totalSteps, completedSteps);
		}

		ScheduleStepTemplate nextStep = templateRepository.findByStepOrder(nextOrder)
				.orElseThrow(() -> new RuntimeException("No template found for step order: " + nextOrder));

		return new NextScheduleStepDTO(nextOrder, nextStep.getStepName(), false, totalSteps, completedSteps);
	}

	@Override
	@Transactional
	public ScheduleResponseDTO createScheduleStep(ScheduleRequestDTO dto) {

		if (dto.getAdmissionCode() == null || dto.getStepOrder() == null || dto.getStartDate() == null
				|| dto.getEndDate() == null) {
			throw new IllegalArgumentException("Missing required fields");
		}

		if (!dto.getEndDate().isAfter(dto.getStartDate())) {
			throw new IllegalArgumentException("End date must be after start date");
		}

		AdmissionWindow admissionWindow = admissionWindowRepository.findByAdmissionCode(dto.getAdmissionCode())
				.orElseThrow(
						() -> new IllegalArgumentException("Admission window not found: " + dto.getAdmissionCode()));

		ScheduleStepTemplate template = templateRepository.findByStepOrder(dto.getStepOrder())
				.orElseThrow(() -> new IllegalArgumentException("Template not found for step: " + dto.getStepOrder()));

		NextScheduleStepDTO nextStep = getNextAvailableStep(admissionWindow.getAdmissionId());

		if (nextStep.isAllStepsCompleted()) {
			throw new IllegalStateException("All steps already completed");
		}

		if (!dto.getStepOrder().equals(nextStep.getNextStepOrder())) {
			throw new IllegalStateException("Invalid step order. Expected: " + nextStep.getNextStepOrder());
		}

		// 🔴 Prevent duplicate
		if (scheduleRepository.existsByAdmissionWindowAndStepOrder(admissionWindow, template.getStepOrder())) {
			throw new IllegalStateException("Step already exists");
		}

		validateDates(dto, admissionWindow, template.getCategory());

		Schedule schedule = new Schedule();
		schedule.setAdmissionWindow(admissionWindow);
		schedule.setTemplate(template);
		schedule.setStepOrder(template.getStepOrder());
		schedule.setStepName(template.getStepName());
		schedule.setCategory(template.getCategory());
		schedule.setDescription(dto.getDescription());
		schedule.setStartDate(dto.getStartDate());
		schedule.setEndDate(dto.getEndDate());
		schedule.setAdmissionRoute(template.getAdmissionRoute());
		schedule.setPhaseNumber(template.getPhaseNumber());

		return convertToDTO(scheduleRepository.save(schedule));
	}

	@Override
	public ScheduleResponseDTO updateScheduleStep(Long scheduleId, ScheduleRequestDTO dto) {
		Schedule schedule = scheduleRepository.findById(scheduleId)
				.orElseThrow(() -> new RuntimeException("Schedule not found"));

		// ==========================================
		// NEW: STATE-AWARE LOCKING LOGIC (BACKEND)
		// ==========================================
		String status = calculateStatus(schedule);

		// 1. REJECT IF EXPIRED
		if ("expired".equals(status)) {
			throw new IllegalStateException("Cannot edit a schedule step that has already closed.");
		}

		// 2. ENFORCE RULES IF ONGOING
		if ("ongoing".equals(status)) {
			// They cannot change the start date by even a millisecond
			if (!schedule.getStartDate().isEqual(dto.getStartDate())) {
				throw new IllegalStateException("This step is currently active. You cannot change its start date.");
			}

			// Ensure they aren't setting the end date to the past
			if (dto.getEndDate().isBefore(LocalDateTime.now())) {
				throw new IllegalStateException("You cannot set the end date of an active step to the past.");
			}
		}

		// VALIDATION against Window based on Category
		validateDates(dto, schedule.getAdmissionWindow(), schedule.getCategory());

		schedule.setDescription(dto.getDescription());
		schedule.setStartDate(dto.getStartDate());
		schedule.setEndDate(dto.getEndDate());
		schedule.setAdmissionRoute(dto.getAdmissionRoute());
		schedule.setPhaseNumber(dto.getPhaseNumber());

		return convertToDTO(scheduleRepository.save(schedule));
	}

	@Override
	public void deleteScheduleStep(Long scheduleId) {
		Schedule schedule = scheduleRepository.findById(scheduleId)
				.orElseThrow(() -> new RuntimeException("Schedule not found"));

		// ==========================================
		// NEW: PREVENT DELETION IF ACTIVE/EXPIRED
		// ==========================================
		String status = calculateStatus(schedule);
		if (!"upcoming".equals(status)) {
			throw new IllegalStateException("You can only delete schedule steps that have not started yet.");
		}

		Optional<Integer> maxStepOrder = scheduleRepository
				.findMaxStepOrderByAdmissionWindow(schedule.getAdmissionWindow().getAdmissionId());

		if (maxStepOrder.isPresent() && !schedule.getStepOrder().equals(maxStepOrder.get())) {
			throw new RuntimeException("Can only delete the most recent schedule step. Delete steps in reverse order.");
		}
		scheduleRepository.delete(schedule);
	}

	@Override
	public ScheduleResponseDTO getScheduleById(Long scheduleId) {
		Schedule schedule = scheduleRepository.findById(scheduleId)
				.orElseThrow(() -> new RuntimeException("Schedule not found"));
		return convertToDTO(schedule);
	}

	// --- UPDATED VALIDATION LOGIC ---
	private void validateDates(ScheduleRequestDTO dto, AdmissionWindow window, String category) {
		if (!dto.getStartDate().isBefore(dto.getEndDate())) {
			throw new RuntimeException("Start date must be before end date");
		}

		if ("PRE_ADMISSION".equalsIgnoreCase(category)) {
			// PRE_ADMISSION must end before Application Window starts
			if (dto.getEndDate().isAfter(window.getStartDate())) {
				throw new RuntimeException("Pre-Admission steps must end before the admission window starts ("
						+ window.getStartDate() + ")");
			}
		} else if ("COUNSELLING".equalsIgnoreCase(category)) {
			// COUNSELLING must start after Application Window ends
			if (dto.getStartDate().isBefore(window.getEndDate())) {
				throw new RuntimeException(
						"Counselling steps must start after the admission window ends (" + window.getEndDate() + ")");
			}
		}
	}

	private ScheduleResponseDTO convertToDTO(Schedule schedule) {
		ScheduleResponseDTO dto = new ScheduleResponseDTO();
		dto.setScheduleId(schedule.getScheduleId());

		// CHANGED: Use the string code
		dto.setAdmissionCode(schedule.getAdmissionWindow().getAdmissionCode());

		dto.setCategory(schedule.getCategory());

		String streamName = (schedule.getAdmissionWindow().getStream() != null)
		        ? schedule.getAdmissionWindow().getStream().getStreamName()
		        : "All Streams";

		String windowName = streamName + " - "
		        + schedule.getAdmissionWindow().getProgrammeLevel() + " - "
		        + schedule.getAdmissionWindow().getSession();

		dto.setAdmissionWindowName(windowName);

		dto.setStepOrder(schedule.getStepOrder());
		dto.setStepName(schedule.getStepName());
		dto.setDescription(schedule.getDescription());
		dto.setStartDate(schedule.getStartDate());
		dto.setEndDate(schedule.getEndDate());
		dto.setStatus(calculateStatus(schedule));

		// Ensure Route and Phase are safely returned to UI
		dto.setAdmissionRoute(schedule.getAdmissionRoute());
		dto.setPhaseNumber(schedule.getPhaseNumber());

		return dto;
	}

	private String calculateStatus(Schedule schedule) {
		LocalDateTime now = LocalDateTime.now();
		if (now.isBefore(schedule.getStartDate()))
			return "upcoming";
		if (now.isAfter(schedule.getEndDate()))
			return "expired";
		return "ongoing";
	}
}