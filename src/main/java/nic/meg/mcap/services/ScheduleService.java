package nic.meg.mcap.services;

import java.util.List;
import nic.meg.mcap.dto.request.ScheduleRequestDTO;
import nic.meg.mcap.dto.response.ScheduleResponseDTO;
import nic.meg.mcap.dto.response.NextScheduleStepDTO;
import nic.meg.mcap.dto.response.ScheduleNotificationResponseDTO;

public interface ScheduleService {

    List<ScheduleResponseDTO> getSchedulesByAdmissionWindow(Short admissionId);

    // --- NEW: Filter by Category ---
    List<ScheduleResponseDTO> getSchedulesByWindowAndCategory(Short admissionId, String category);

    NextScheduleStepDTO getNextAvailableStep(Short admissionId);

    ScheduleResponseDTO createScheduleStep(ScheduleRequestDTO dto);

    ScheduleResponseDTO updateScheduleStep(Long scheduleId, ScheduleRequestDTO dto);

    void deleteScheduleStep(Long scheduleId);

    ScheduleResponseDTO getScheduleById(Long scheduleId);

    List<ScheduleNotificationResponseDTO> getInstituteScheduleNotifications();
}