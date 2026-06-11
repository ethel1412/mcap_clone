package nic.meg.mcap.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SeatMatrixResponseDTO {
    private Long id;
    private Integer programmeOfferedId; // Use Integer
    private Integer totalSeats;
}