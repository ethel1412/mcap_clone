package nic.meg.mcap.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SeatMatrixRequestDTO {
    private Integer programmeOfferedId;
    private Integer totalSeats;
    private Short admissionWindowId;
}