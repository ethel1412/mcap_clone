package nic.meg.mcap.services;

import java.time.LocalDateTime;
import java.util.List;

import nic.meg.mcap.dto.response.UpcomingAdmissionWindowResponseDTO;
import nic.meg.mcap.entities.AdmissionWindow;
import nic.meg.mcap.repositories.AdmissionWindowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UpcomingAdmissionWindowQueryService {

    @Autowired
    private AdmissionWindowRepository admissionWindowRepository;

    private String computeStatus(AdmissionWindow window, LocalDateTime now) {
        if (!window.isActive()) {
            return "INACTIVE";
        }
        if (now.isBefore(window.getStartDate())) {
            return "UPCOMING";
        }
        if (now.isAfter(window.getEndDate())) {
            return "EXPIRED";
        }
        return "ACTIVE";
    }

    public List<UpcomingAdmissionWindowResponseDTO> findUpcomingAdmissionWindow() {
        List<AdmissionWindow> all = admissionWindowRepository.findAllWithProgrammes();
        LocalDateTime now = LocalDateTime.now();

        return all.stream()
                .filter(w -> w.isActive() && w.getStartDate().isAfter(now))
                .map(w -> {
                    String status = computeStatus(w, now);
                    return UpcomingAdmissionWindowResponseDTO.builder()
                            .admissionId(w.getAdmissionId())
                            .streamName(w.getStream() != null ? w.getStream().getStreamName() : "All Streams")
                            .programmeLevel(w.getProgrammeLevel().name())
                            .session(w.getSession())
                            .startDate(w.getStartDate() != null ? w.getStartDate().toString() : null)
                            .endDate(w.getEndDate() != null ? w.getEndDate().toString() : null)
                            .activeFlag(w.isActive())
                            .status(status)
                            .build();
                })
                .toList();
    }
}