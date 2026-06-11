package nic.meg.mcap.repositories;

import nic.meg.mcap.entities.ApplicantSubjectPreference;
import nic.meg.mcap.entities.SeatAllotment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ApplicantSubjectPreferenceRepository extends JpaRepository<ApplicantSubjectPreference, Long> {
    void deleteBySeatAllotment(SeatAllotment seatAllotment);
    List<ApplicantSubjectPreference> findBySeatAllotment(SeatAllotment seatAllotment);
}