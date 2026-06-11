package nic.meg.mcap.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nic.meg.mcap.enums.ProgrammeLevel;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdmissionCriteriaRequestDTO {

    @NotNull(message = "Admission window is required")
    private Short admissionWindowId;

    /**
     * Kept for compatibility (since you said do not remove stream yet).
     * You can keep sending it from UI as null for UG if you want programme-wise only.
     */
    private Short streamId;

    /**
     * Programme is required for both UG and PG (UG is programme-wise now).
     */
    @NotNull(message = "Programme is required")
    private Short programmeId;

    @NotNull(message = "Programme level is required")
    private ProgrammeLevel programmeLevel;

    /**
     * Selected CUET subjects for merit (must be subset of EligibilityCriteria CUET subjects).
     * Empty/null means: use overallPercentile later during merit generation.
     */
    private List<String> cuetMeritSubjects = new ArrayList<>();

    /**
     * Selected qualification subjects for merit (must be subset of Subject master).
     * Empty/null means: use overall AcademicRecord percentage later during merit generation.
     */
    private List<String> nonCuetMeritSubjects = new ArrayList<>();

    /**
     * Tie-breaker ordering.
     * Existing DTO has only: field + priority.
     */
    private List<TieBreakerCriterionDTO> tiebreakerConfig = new ArrayList<>();
}
