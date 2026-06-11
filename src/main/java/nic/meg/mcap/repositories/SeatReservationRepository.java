package nic.meg.mcap.repositories;

import nic.meg.mcap.entities.SeatReservation;
import nic.meg.mcap.enums.ApplicantType;
import nic.meg.mcap.enums.ReservationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeatReservationRepository extends JpaRepository<SeatReservation, Long> {

    List<SeatReservation> findByProgrammeOfferedProgrammeOfferedId(Integer programmeOfferedId);

    // Existing: total reserved for a ProgrammeOffered across all windows/types
    @Query("""
           SELECT COALESCE(SUM(sr.reservedSeats), 0)
           FROM SeatReservation sr
           WHERE sr.programmeOffered.programmeOfferedId = :programmeOfferedId
           """)
    Integer getTotalReservedSeats(@Param("programmeOfferedId") Integer programmeOfferedId);

    // NEW: total reserved scoped to programmeOffered + admissionWindow + applicantType
    @Query("""
           SELECT COALESCE(SUM(sr.reservedSeats), 0)
           FROM SeatReservation sr
           WHERE sr.programmeOffered.programmeOfferedId = :programmeOfferedId
             AND sr.admissionWindow.admissionId = :admissionWindowId
             AND sr.applicantType = :applicantType
           """)
    Integer getTotalReservedSeatsByProgrammeOfferedAndWindowAndApplicantType(
            @Param("programmeOfferedId") Integer programmeOfferedId,
            @Param("admissionWindowId") Short admissionWindowId,
            @Param("applicantType") ApplicantType applicantType
    );

    // Existing uniqueness (not window/type scoped) - keep if you still need it somewhere
    Optional<SeatReservation> findByProgrammeOfferedProgrammeOfferedIdAndReservationTypeAndCommunityCategoryCategoryCode(
            Integer programmeOfferedId,
            ReservationType reservationType,
            String categoryCode
    );

    Optional<SeatReservation> findByProgrammeOfferedProgrammeOfferedIdAndReservationType(
            Integer programmeOfferedId,
            ReservationType reservationType
    );

    //  programmeOffered + window + applicantType + reservationType
    Optional<SeatReservation> findByProgrammeOfferedProgrammeOfferedIdAndAdmissionWindowAdmissionIdAndApplicantTypeAndReservationType(
            Integer programmeOfferedId,
            Short admissionWindowId,
            ApplicantType applicantType,
            ReservationType reservationType
    );

    //  programmeOffered + window + applicantType + reservationType + categoryCode
    Optional<SeatReservation> findByProgrammeOfferedProgrammeOfferedIdAndAdmissionWindowAdmissionIdAndApplicantTypeAndReservationTypeAndCommunityCategoryCategoryCode(
            Integer programmeOfferedId,
            Short admissionWindowId,
            ApplicantType applicantType,
            ReservationType reservationType,
            String categoryCode
    );

    void deleteByProgrammeOfferedProgrammeOfferedIdAndId(Integer programmeOfferedId, Long id);

    @Query("""
           SELECT sr
           FROM SeatReservation sr
           WHERE sr.programmeOffered.programmeOfferedId = :programmeOfferedId
             AND sr.admissionWindow.admissionId = :admissionWindowId
           """)
    List<SeatReservation> findByProgrammeOfferedIdAndAdmissionWindowId(
            @Param("programmeOfferedId") Integer programmeOfferedId,
            @Param("admissionWindowId") Short admissionWindowId
    );
}
