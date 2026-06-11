package nic.meg.mcap.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import nic.meg.mcap.enums.Shift;
import java.util.List;

@Data
public class AvailableSubjectsRequestDTO {
    @NotNull
    private Integer programmeOfferedId;
    @NotNull
    private Shift shift;

    private List<Integer> minorSubjectIds;
    private List<Integer> mdcSubjectIds;
    private List<Integer> aecSubjectIds;
    private List<Integer> secSubjectIds;
    private List<Integer> vacSubjectIds;
}