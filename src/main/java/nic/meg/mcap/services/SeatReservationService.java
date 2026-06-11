package nic.meg.mcap.services;

import nic.meg.mcap.dto.request.SeatReservationRequestDTO;
import nic.meg.mcap.dto.response.SeatReservationResponseDTO;
import nic.meg.mcap.entities.SeatReservation;

import java.util.List;

public interface SeatReservationService {

    SeatReservationResponseDTO createReservation(SeatReservationRequestDTO requestDTO);

    List<SeatReservationResponseDTO> getReservationsByProgrammeOffered(Integer programmeOfferedId);

    void deleteReservation(Long id, Integer programmeOfferedId);

    Integer getTotalReservedSeats(Integer programmeOfferedId);

    List<SeatReservationResponseDTO> getReservationsByProgrammeAndWindow(Integer programmeOfferedId, Short admissionWindowId);

    SeatReservationResponseDTO updateReservation(Long reservationId, SeatReservationRequestDTO requestDTO);


    default List<SeatReservation> getAllReservationsByProgrammeOfferedId(Integer programmeOfferedId) {
        // This will be overridden in the implementation
        throw new UnsupportedOperationException("Method must be implemented");
    }
}