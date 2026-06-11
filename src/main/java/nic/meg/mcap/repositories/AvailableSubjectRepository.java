package nic.meg.mcap.repositories;

import nic.meg.mcap.entities.AvailableSubject;
import nic.meg.mcap.entities.ProgrammeOffered;
import nic.meg.mcap.enums.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface AvailableSubjectRepository extends JpaRepository<AvailableSubject, Long> {

    List<AvailableSubject> findByProgrammeOffered(ProgrammeOffered programmeOffered);

    List<AvailableSubject> findByProgrammeOfferedAndShift(ProgrammeOffered programmeOffered, Shift shift);

    @Transactional
    void deleteByProgrammeOfferedAndShift(ProgrammeOffered programmeOffered, Shift shift);
}