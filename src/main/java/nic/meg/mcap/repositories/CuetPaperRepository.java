package nic.meg.mcap.repositories;

import nic.meg.mcap.entities.CuetPaper;
import nic.meg.mcap.enums.ProgrammeLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CuetPaperRepository extends JpaRepository<CuetPaper, Long> {
    List<CuetPaper> findByProgrammeLevelAndIsActiveOrderBySpecAscSortOrderAscPaperNameAsc(ProgrammeLevel programmeLevel, boolean isActive);
    Optional<CuetPaper> findByProgrammeLevelAndPaperCode(ProgrammeLevel programmeLevel, String paperCode);
}
