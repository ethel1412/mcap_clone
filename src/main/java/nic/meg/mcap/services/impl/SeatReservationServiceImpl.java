package nic.meg.mcap.services.impl;

import nic.meg.mcap.dto.request.SeatReservationRequestDTO;
import nic.meg.mcap.dto.response.SeatReservationResponseDTO;
import nic.meg.mcap.entities.*;
import nic.meg.mcap.enums.ReservationType;
import nic.meg.mcap.enums.ApplicantType;
import nic.meg.mcap.enums.ScoreSource;
import nic.meg.mcap.repositories.*;
import nic.meg.mcap.services.SeatReservationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class SeatReservationServiceImpl implements SeatReservationService {

    private final SeatReservationRepository reservationRepository;
    private final ProgrammesOfferedRepository programmeOfferedRepository;
    private final CommunityCategoryRepository communityCategoryRepository;
    private final SeatMatrixRepository seatMatrixRepository;
    private final AdmissionWindowRepository admissionWindowRepository;

    public SeatReservationServiceImpl(
            SeatReservationRepository reservationRepository,
            ProgrammesOfferedRepository programmeOfferedRepository,
            CommunityCategoryRepository communityCategoryRepository,
            SeatMatrixRepository seatMatrixRepository,
            AdmissionWindowRepository admissionWindowRepository) {
        this.reservationRepository = reservationRepository;
        this.programmeOfferedRepository = programmeOfferedRepository;
        this.communityCategoryRepository = communityCategoryRepository;
        this.seatMatrixRepository = seatMatrixRepository;
        this.admissionWindowRepository = admissionWindowRepository;
    }

    @Override
    @Transactional
    public SeatReservationResponseDTO createReservation(SeatReservationRequestDTO requestDTO) {

        AdmissionWindow admissionWindow = admissionWindowRepository.findById(requestDTO.getAdmissionWindowId())
                .orElseThrow(() -> new EntityNotFoundException("Admission window not found"));

        ProgrammeOffered programmeOffered = programmeOfferedRepository.findById(requestDTO.getProgrammeOfferedId())
                .orElseThrow(() -> new EntityNotFoundException("Programme not found"));

        SeatMatrix seatMatrix = seatMatrixRepository
                .findByProgrammeOfferedProgrammeOfferedId(requestDTO.getProgrammeOfferedId())
                .orElseThrow(() -> new IllegalStateException("Please allocate total seats before reserving"));

        ApplicantType applicantType = requestDTO.getApplicantType();
        if (applicantType == null) {
            throw new IllegalArgumentException("Applicant type is required");
        }

        BigDecimal percentage = requestDTO.getReservedPercentage();
        if (percentage == null || percentage.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Reserved percentage must be greater than 0");
        }
        if (percentage.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("Reserved percentage cannot exceed 100");
        }

        int totalSeats = seatMatrix.getTotalSeats();

        // seats = floor(totalSeats * percentage / 100)
        int seatsForThisReservation = percentage
                .multiply(BigDecimal.valueOf(totalSeats))
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN)
                .intValueExact();

        if (seatsForThisReservation < 1) {
            throw new IllegalArgumentException("Percentage is too small; results in 0 seats");
        }

        // total‑seats guard using derived seats
        Integer currentReserved = reservationRepository.getTotalReservedSeats(requestDTO.getProgrammeOfferedId());
        int newTotal = currentReserved + seatsForThisReservation;
        if (newTotal > totalSeats) {
            throw new IllegalStateException(
                    String.format("Cannot reserve %.2f%% (%d seats). Total seats: %d, Already reserved: %d, Available: %d",
                            percentage,
                            seatsForThisReservation,
                            totalSeats,
                            currentReserved,
                            totalSeats - currentReserved)
            );
        }

        // Uniqueness checks
        if (requestDTO.getReservationType() == ReservationType.EXAM_QUALIFIED ||
                requestDTO.getReservationType() == ReservationType.COMMUNITY) {

            if (requestDTO.getCategoryCode() == null) {
                throw new IllegalArgumentException("Category selection is required for this reservation type");
            }

            reservationRepository.findByProgrammeOfferedProgrammeOfferedIdAndReservationTypeAndCommunityCategoryCategoryCode(
                    requestDTO.getProgrammeOfferedId(),
                    requestDTO.getReservationType(),
                    requestDTO.getCategoryCode()
            ).ifPresent(existing -> {
                throw new IllegalStateException("Reservation already exists for this Category under this Type");
            });

        } else {
            reservationRepository.findByProgrammeOfferedProgrammeOfferedIdAndReservationType(
                    requestDTO.getProgrammeOfferedId(),
                    requestDTO.getReservationType()
            ).ifPresent(existing -> {
                throw new IllegalStateException("Reservation already exists for " + requestDTO.getReservationType());
            });
        }

        // create entity with applicant type + exam + percentage + derived seats
        SeatReservation reservation = new SeatReservation();
        reservation.setAdmissionWindow(admissionWindow);
        reservation.setProgrammeOffered(programmeOffered);
        reservation.setReservationType(requestDTO.getReservationType());
        reservation.setApplicantType(applicantType);
        reservation.setReservedPercentage(percentage);
        reservation.setReservedSeats(seatsForThisReservation);
        reservation.setExamSource(requestDTO.getExamSource());

        if (requestDTO.getReservationType() == ReservationType.EXAM_QUALIFIED ||
                requestDTO.getReservationType() == ReservationType.COMMUNITY) {

            CommunityCategory category = communityCategoryRepository.findById(requestDTO.getCategoryCode())
                    .orElseThrow(() -> new EntityNotFoundException("Category not found"));
            reservation.setCommunityCategory(category);
        }

        SeatReservation saved = reservationRepository.save(reservation);
        return convertToDTO(saved, programmeOffered);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeatReservationResponseDTO> getReservationsByProgrammeOffered(Integer programmeOfferedId) {
        List<SeatReservation> reservations = reservationRepository
                .findByProgrammeOfferedProgrammeOfferedId(programmeOfferedId);

        return reservations.stream()
                .map(r -> convertToDTO(r, r.getProgrammeOffered()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteReservation(Long id, Integer programmeOfferedId) {
        SeatReservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reservation not found"));

        // Verify ownership
        if (!reservation.getProgrammeOffered().getProgrammeOfferedId().equals(programmeOfferedId)) {
            throw new IllegalStateException("Unauthorized access");
        }

        reservationRepository.delete(reservation);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getTotalReservedSeats(Integer programmeOfferedId) {
        return reservationRepository.getTotalReservedSeats(programmeOfferedId);
    }

    private SeatReservationResponseDTO convertToDTO(SeatReservation reservation, ProgrammeOffered programmeOffered) {
        SeatReservationResponseDTO dto = new SeatReservationResponseDTO();
        dto.setId(reservation.getId());
        dto.setProgrammeOfferedId(programmeOffered.getProgrammeOfferedId());
        dto.setReservationType(reservation.getReservationType());
        dto.setReservedSeats(reservation.getReservedSeats());
        dto.setApplicantType(reservation.getApplicantType());
        dto.setReservedPercentage(reservation.getReservedPercentage());

        // exam info
        dto.setExamSource(reservation.getExamSource());
        if (reservation.getExamSource() != null) {
            dto.setExamDisplayName(buildExamDisplayName(reservation.getExamSource()));
        }

        // Category / title logic
        if (reservation.getCommunityCategory() != null) {
            dto.setCategoryCode(reservation.getCommunityCategory().getCategoryCode());
            dto.setCategoryName(reservation.getCommunityCategory().getCategoryName());

            if (reservation.getReservationType() == ReservationType.EXAM_QUALIFIED) {
                String base = dto.getExamDisplayName() != null
                        ? dto.getExamDisplayName()
                        : "Entrance exam";
                dto.setDisplayTitle(base + " (" + reservation.getCommunityCategory().getCategoryName() + ")");
            } else {
                dto.setDisplayTitle(reservation.getCommunityCategory().getCategoryName() + " Category");
            }
        } else {
            if (dto.getExamDisplayName() != null) {
                dto.setDisplayTitle(dto.getExamDisplayName());
            } else {
                dto.setDisplayTitle(reservation.getReservationType().toString());
            }
        }

        return dto;
    }
    private String buildExamDisplayName(ScoreSource source) {
        if (source == null) return null;
    
        return switch (source) {
            case QUALIFICATION_EXAM -> "Qualification Exam";
            case NON_CUET -> "NON-CUET";
            case CUET -> "CUET";
            case JEE -> "JEE Main";
            case GATE -> "GATE";
            case NET -> "NET";
            case OTHER -> "Other Entrance Exam";
            default -> source.name(); // keeps code future-proof if enum gets new values
        };
    }
    


    @Override
    @Transactional(readOnly = true)
    public List<SeatReservation> getAllReservationsByProgrammeOfferedId(Integer programmeOfferedId) {
        return reservationRepository.findByProgrammeOfferedProgrammeOfferedId(programmeOfferedId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeatReservationResponseDTO> getReservationsByProgrammeAndWindow(Integer programmeOfferedId, Short admissionWindowId) {
        List<SeatReservation> reservations = reservationRepository
                .findByProgrammeOfferedIdAndAdmissionWindowId(programmeOfferedId, admissionWindowId);
        return reservations.stream()
                .map(r -> convertToDTO(r, r.getProgrammeOffered()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SeatReservationResponseDTO updateReservation(Long reservationId, SeatReservationRequestDTO requestDTO) {
        SeatReservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new EntityNotFoundException("Seat reservation not found with ID: " + reservationId));

        ApplicantType applicantType = requestDTO.getApplicantType();
        if (applicantType == null) {
            throw new IllegalArgumentException("Applicant type is required");
        }

        BigDecimal percentage = requestDTO.getReservedPercentage();
        if (percentage == null || percentage.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Reserved percentage must be greater than 0");
        }

        SeatMatrix seatMatrix = seatMatrixRepository
                .findByProgrammeOfferedProgrammeOfferedId(requestDTO.getProgrammeOfferedId())
                .orElseThrow(() -> new IllegalStateException("Please allocate total seats before reserving"));

        int totalSeats = seatMatrix.getTotalSeats();
        int seatsForThisReservation = percentage
                .multiply(BigDecimal.valueOf(totalSeats))
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN)
                .intValueExact();

        reservation.setApplicantType(applicantType);
        reservation.setReservedPercentage(percentage);
        reservation.setReservedSeats(seatsForThisReservation);
        reservation.setReservationType(requestDTO.getReservationType());
        reservation.setExamSource(requestDTO.getExamSource());

        if (requestDTO.getCategoryCode() != null) {
            CommunityCategory category = communityCategoryRepository.findById(requestDTO.getCategoryCode())
                    .orElseThrow(() -> new EntityNotFoundException("Community category not found"));
            reservation.setCommunityCategory(category);
        } else {
            reservation.setCommunityCategory(null);
        }

        SeatReservation updated = reservationRepository.save(reservation);
        return convertToDTO(updated, updated.getProgrammeOffered());
    }

}
