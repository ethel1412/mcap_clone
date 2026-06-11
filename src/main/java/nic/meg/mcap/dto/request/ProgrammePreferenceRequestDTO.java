package nic.meg.mcap.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

@Data
public class ProgrammePreferenceRequestDTO {

    @NotNull(message = "Application ID is required")
    private Long applicationId;

    @NotNull(message = "At least one preference is required")
    @Size(min = 1, max = 10, message = "You can select between 1 and 10 preferences")
    @Valid
    private List<PreferenceItemDTO> preferences;

    @Data
    public static class PreferenceItemDTO {

        // THE GOLDEN CHANGE: This replaces both programmeId and instituteId
        @NotNull(message = "Programme Offered selection is required")
        private Integer programmeOfferedId;

        @NotNull(message = "Preference order is required")
        private Integer preferenceOrder;

        private List<String> minorCoursePreferences;
        private List<String> mdcCoursePreferences;
        private List<String> aecCoursePreferences;
        private List<String> secCoursePreferences;
        private String valueAddedCourse;
    }
}