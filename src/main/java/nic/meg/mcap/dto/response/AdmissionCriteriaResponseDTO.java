package nic.meg.mcap.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nic.meg.mcap.dto.request.TieBreakerCriterionDTO;
import nic.meg.mcap.enums.ProgrammeLevel;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdmissionCriteriaResponseDTO {

    private Long criteriaId;

    private Short admissionWindowId;
    private String admissionWindowName;

    // Kept for compatibility (you said don't remove stream yet)
    private Short streamId;
    private String streamName;

    private Short programmeId;
    private String programmeName;

    private ProgrammeLevel programmeLevel;

    private boolean isActive;

    /**
     * Selected CUET subjects used for merit.
     * Empty => later merit calculation uses CuetScore.overallPercentile.
     */
    @Builder.Default
    private List<String> cuetMeritSubjects = new ArrayList<>();

    /**
     * Selected non-CUET qualification subjects used for merit.
     * Empty => later merit calculation uses AcademicRecord.percentage.
     */
    @Builder.Default
    private List<String> nonCuetMeritSubjects = new ArrayList<>();

    /**
     * Tie-breaker ordering config (JSON-backed in entity).
     */
    @Builder.Default
    private List<TieBreakerCriterionDTO> tiebreakerConfig = new ArrayList<>();
}
