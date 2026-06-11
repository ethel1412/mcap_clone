package nic.meg.mcap.services;

import java.util.List;
import nic.meg.mcap.dto.response.ActiveAdmissionWindowResponseDTO;
import nic.meg.mcap.enums.StepPreset;

public interface ScheduleHelperService {

    /**
     * Find admission windows currently in a specific schedule step
     * @param stepName The name of the schedule step (e.g., "Merit List Generation")
     * @return List of active windows in that step
     */
    List<ActiveAdmissionWindowResponseDTO> findWindowsInScheduleStep(String stepName);

    /**
     * @deprecated Use findWindowsInScheduleStep(String stepName) instead.
     * All step names now come from ScheduleStepTemplate table.
     */
    @Deprecated
    List<ActiveAdmissionWindowResponseDTO> findWindowsInScheduleStep(StepPreset stepPreset);

    /**
     * Check if a specific admission window is currently in a given schedule step
     */
    // CHANGED: Short admissionId to String admissionCode
    boolean isWindowInScheduleStep(String admissionCode, String stepName);

    /**
     * Get the current active schedule step for an admission window
     */
    // CHANGED: Short admissionId to String admissionCode
    String getCurrentScheduleStep(String admissionCode);
}