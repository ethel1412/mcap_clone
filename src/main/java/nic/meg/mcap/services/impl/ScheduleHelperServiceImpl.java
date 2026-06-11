package nic.meg.mcap.services.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import nic.meg.mcap.dto.response.ActiveAdmissionWindowResponseDTO;
import nic.meg.mcap.entities.AdmissionWindow;
import nic.meg.mcap.entities.Schedule;
import nic.meg.mcap.enums.StepPreset;
import nic.meg.mcap.repositories.ScheduleRepository;
import nic.meg.mcap.services.ScheduleHelperService;

@Service
public class ScheduleHelperServiceImpl implements ScheduleHelperService {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Override
    public List<ActiveAdmissionWindowResponseDTO> findWindowsInScheduleStep(String stepName) {
        LocalDateTime now = LocalDateTime.now();

        // Get all schedules matching the step name and active time range
        List<Schedule> activeSchedules = scheduleRepository.findAll().stream()
                .filter(s -> s.getStepName().equalsIgnoreCase(stepName)
                        && s.getStartDate().isBefore(now)
                        && s.getEndDate().isAfter(now))
                .collect(Collectors.toList());

        // Extract unique admission windows
        List<AdmissionWindow> windows = activeSchedules.stream()
                .map(Schedule::getAdmissionWindow)
                .distinct()
                .filter(AdmissionWindow::isActive)
                .collect(Collectors.toList());

        // Convert to DTO using the new architecture fields
        return windows.stream()
                .map(window -> {
                    Schedule schedule = activeSchedules.stream()
                            .filter(s -> s.getAdmissionWindow().getAdmissionId() == window.getAdmissionId())
                            .findFirst()
                            .orElse(null);

                    ActiveAdmissionWindowResponseDTO dto = new ActiveAdmissionWindowResponseDTO();

                    // CHANGED: Expose code to frontend instead of the internal ID
                    dto.setAdmissionCode(window.getAdmissionCode());

                    dto.setStreamName(window.getStream() != null ? window.getStream().getStreamName() : "All Streams");
                    dto.setProgrammeLevel(window.getProgrammeLevel().name());
                    dto.setSession(window.getSession());
                    dto.setStartDate(window.getStartDate().toString());
                    dto.setEndDate(window.getEndDate().toString());
                    dto.setStatus(stepName + " Phase");

                    // Populate new extension fields
                    dto.setExtended(window.isExtended());
                    dto.setOriginalEndDate(window.getOriginalEndDate() != null ? window.getOriginalEndDate().toString() : null);

                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Deprecated
    public List<ActiveAdmissionWindowResponseDTO> findWindowsInScheduleStep(StepPreset stepPreset) {
        return findWindowsInScheduleStep(stepPreset.name());
    }

    // CHANGED: Short admissionId to String admissionCode
    @Override
    public boolean isWindowInScheduleStep(String admissionCode, String stepName) {
        LocalDateTime now = LocalDateTime.now();
        return scheduleRepository.findAll().stream()
                .anyMatch(s -> s.getAdmissionWindow().getAdmissionCode().equals(admissionCode)
                        && s.getStepName().equalsIgnoreCase(stepName)
                        && s.getStartDate().isBefore(now)
                        && s.getEndDate().isAfter(now));
    }

    // CHANGED: Short admissionId to String admissionCode
    @Override
    public String getCurrentScheduleStep(String admissionCode) {
        LocalDateTime now = LocalDateTime.now();
        return scheduleRepository.findAll().stream()
                .filter(s -> s.getAdmissionWindow().getAdmissionCode().equals(admissionCode)
                        && s.getStartDate().isBefore(now)
                        && s.getEndDate().isAfter(now))
                .map(Schedule::getStepName)
                .findFirst()
                .orElse(null);
    }
}