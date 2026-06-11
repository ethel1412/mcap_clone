package nic.meg.mcap.services.impl;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import nic.meg.mcap.dto.request.AdmissionWindowRequestDTO;
import nic.meg.mcap.dto.response.ActiveAdmissionWindowResponseDTO;
import nic.meg.mcap.dto.response.AdmissionWindowProgrammeResponseDTO;
import nic.meg.mcap.entities.AdmissionWindow;
import nic.meg.mcap.entities.AdmissionWindowProgramme;
import nic.meg.mcap.entities.Programme;
import nic.meg.mcap.entities.Stream;
import nic.meg.mcap.enums.ProgrammeLevel;
import nic.meg.mcap.repositories.AdmissionWindowProgrammeRepository;
import nic.meg.mcap.repositories.AdmissionWindowRepository;
import nic.meg.mcap.repositories.ProgrammeRepository;
import nic.meg.mcap.repositories.StreamRepository;
import nic.meg.mcap.services.AdmissionWindowService;

@Service
public class AdmissionWindowServiceImpl implements AdmissionWindowService {

    @Autowired
    private AdmissionWindowRepository admissionWindowRepository;

    @Autowired
    private StreamRepository streamRepository;

    @Autowired
    private ProgrammeRepository programmeRepository;

    @Autowired
    private AdmissionWindowProgrammeRepository admissionWindowProgrammeRepository;

    private static final Logger logger = LoggerFactory.getLogger(AdmissionWindowServiceImpl.class);

    @Override
    public List<Stream> getAllStreams() {
        return streamRepository.findAll();
    }

    @Override
    public List<AdmissionWindow> getAllAdmissionWindowsWithProgrammes() {
        return admissionWindowRepository.findAllWithProgrammes();
    }

    @Override
    public List<AdmissionWindow> getWindowsByStatus(String status) {
        List<AdmissionWindow> allWindows = this.getAllAdmissionWindowsWithProgrammes();
        LocalDateTime now = LocalDateTime.now();

        if (status == null) {
            return allWindows;
        }

        switch (status.toLowerCase()) {
            case "active":
                return admissionWindowRepository.findActiveWindows(now);
            case "upcoming":
                return admissionWindowRepository.findUpcomingWindows(now);
            case "closed":
                return admissionWindowRepository.findClosedWindowsWaitingForCounselling(now);
            default:
                return Collections.emptyList();
        }
    }

    @Override
    @Transactional
    public void saveAdmissionWindow(AdmissionWindowRequestDTO dto) {

        Stream stream = null;

        if (dto.getStreamId() != null) {
            stream = streamRepository.findById(dto.getStreamId())
                    .orElseThrow(() -> new EntityNotFoundException("Stream not found"));
        }

        boolean exists;

        if (stream != null) {
            exists = admissionWindowRepository.existsByStreamAndProgrammeLevelAndSessionAndStartDateAndEndDate(stream,
                    dto.getProgrammeLevel(), dto.getSession(), dto.getStartDate(), dto.getEndDate());
        } else {
            // 👉 You need a separate method for null stream
            exists = admissionWindowRepository.existsByStreamIsNullAndProgrammeLevelAndSessionAndStartDateAndEndDate(
                    dto.getProgrammeLevel(), dto.getSession(), dto.getStartDate(), dto.getEndDate());
        }

        if (exists) {
            throw new IllegalStateException("An admission window already exists for this combination.");
        }

        // ✅ Create window
        AdmissionWindow window = new AdmissionWindow();
        window.setStream(stream);
        window.setProgrammeLevel(dto.getProgrammeLevel());
        window.setSession(dto.getSession());
        window.setStartDate(dto.getStartDate());
        window.setEndDate(dto.getEndDate());
        window.setExtended(false);
        window.setActive(true);

        String code = generateAdmissionCode(dto.getProgrammeLevel(), dto.getSession());
        window.setAdmissionCode(code);

        AdmissionWindow savedWindow = admissionWindowRepository.save(window);
        associateProgrammes(savedWindow, dto);
    }

    private String generateAdmissionCode(ProgrammeLevel level, String session) {
        String year = session.substring(2);
        AdmissionWindow last = admissionWindowRepository.findLatestWithLock(level, session);

        int next = 1;

        if (last != null) {
            String lastCode = last.getAdmissionCode(); // UG26-0007
            String numberPart = lastCode.split("-")[1];
            next = Integer.parseInt(numberPart) + 1;
        }

        return level.name() + year + "-" + String.format("%04d", next);
    }

    private void associateProgrammes(AdmissionWindow window, AdmissionWindowRequestDTO dto) {

        // ✅ ALL STREAMS → skip programme mapping completely
        if (window.getStream() == null) {
            return;
        }

        // ✅ Only for PROGRAMME type
        if ("PROGRAMME".equalsIgnoreCase(dto.getWindowType())) {

            if (dto.getProgrammeIds() == null || dto.getProgrammeIds().isEmpty()) {
                throw new IllegalArgumentException("At least one Programme must be selected.");
            }

            List<AdmissionWindowProgramme> associations = dto.getProgrammeIds().stream().filter(Objects::nonNull) // 🔥
                    // avoid
                    // null
                    // IDs
                    .map(pId -> {
                        Programme p = programmeRepository.findById(pId)
                                .orElseThrow(() -> new EntityNotFoundException("Programme not found: " + pId));
                        return new AdmissionWindowProgramme(null, window, p, true);
                    }).collect(Collectors.toList());

            admissionWindowProgrammeRepository.saveAll(associations);
        }
    }

    // CHANGED: Replaces findById(Short id)
    @Override
    public AdmissionWindow findByCode(String admissionCode) {
        return admissionWindowRepository.findByAdmissionCode(admissionCode).orElseThrow(
                () -> new EntityNotFoundException("Admission Window not found with code: " + admissionCode));
    }

    // CHANGED: Short admissionId to String admissionCode
    @Override
    @Transactional
    public void extendWindow(String admissionCode, LocalDateTime newEndDate) {
        AdmissionWindow window = findByCode(admissionCode);

        if (newEndDate.isBefore(window.getEndDate())) {
            throw new IllegalArgumentException("New end date must be after the current end date.");
        }

        // Store original date only on first extension
        if (!window.isExtended()) {
            window.setOriginalEndDate(window.getEndDate());
            window.setExtended(true);
        }

        window.setEndDate(newEndDate);
        admissionWindowRepository.save(window);
    }

    @Override
    public List<ActiveAdmissionWindowResponseDTO> findActiveAdmissionWindows() {
        LocalDateTime now = LocalDateTime.now();
        return admissionWindowRepository.findActiveWindows(now).stream().map(this::convertToActiveDTO)
                .collect(Collectors.toList());
    }

    private ActiveAdmissionWindowResponseDTO convertToActiveDTO(AdmissionWindow window) {
        ActiveAdmissionWindowResponseDTO dto = new ActiveAdmissionWindowResponseDTO();

        // CHANGED: Expose code to frontend instead of internal ID
        dto.setAdmissionCode(window.getAdmissionCode());

        dto.setStreamName(window.getStream() != null ? window.getStream().getStreamName() : "All Streams");
        dto.setProgrammeLevel(window.getProgrammeLevel().name());
        dto.setSession(window.getSession());
        dto.setStartDate(window.getStartDate().toString());
        dto.setEndDate(window.getEndDate().toString());
        dto.setStatus("Active");

        // Mapped new extension fields
        dto.setExtended(window.isExtended());
        dto.setOriginalEndDate(window.getOriginalEndDate() != null ? window.getOriginalEndDate().toString() : null);

        return dto;
    }

    // CHANGED: Short id to String admissionCode
    @Override
    @Transactional
    public void updateAdmissionWindow(String admissionCode, AdmissionWindowRequestDTO dto) {

        AdmissionWindow window = findByCode(admissionCode);

        // ✅ handle nullable stream safely
        Stream stream = null;
        if (dto.getStreamId() != null) {
            stream = streamRepository.findById(dto.getStreamId())
                    .orElseThrow(() -> new EntityNotFoundException("Stream not found"));
        }

        boolean exists;

        if (stream != null) {
            exists = admissionWindowRepository.existsByStreamAndProgrammeLevelAndSessionAndStartDateAndEndDate(stream,
                    dto.getProgrammeLevel(), dto.getSession(), dto.getStartDate(), dto.getEndDate());
        } else {
            // 👉 You need a separate method for null stream
            exists = admissionWindowRepository.existsByStreamIsNullAndProgrammeLevelAndSessionAndStartDateAndEndDate(
                    dto.getProgrammeLevel(), dto.getSession(), dto.getStartDate(), dto.getEndDate());
        }

        if (exists) {
            throw new IllegalStateException("An admission window already exists for this combination.");
        }

        // ✅ set values
        window.setStream(stream);
        window.setProgrammeLevel(dto.getProgrammeLevel());
        window.setSession(dto.getSession());
        window.setStartDate(dto.getStartDate());
        window.setEndDate(dto.getEndDate());

        // ✅ clear old mappings
        admissionWindowProgrammeRepository.deleteByAdmissionWindow(window);

        // ✅ re-map programmes safely
        associateProgrammes(window, dto);

        // ✅ save
        admissionWindowRepository.save(window);
    }

    // CHANGED: Short id to String admissionCode
    @Override
    public AdmissionWindowRequestDTO getAdmissionWindowForEdit(String admissionCode) {
        AdmissionWindow window = findByCode(admissionCode);
        AdmissionWindowRequestDTO dto = new AdmissionWindowRequestDTO();
        dto.setWindowType(window.getAdmissionWindowProgrammes().isEmpty() ? "STREAM" : "PROGRAMME");

        // CHANGED: Bind code instead of ID
        dto.setAdmissionCode(window.getAdmissionCode());
        dto.setStreamId(window.getStream() != null ? window.getStream().getStreamId() : null);
        dto.setProgrammeLevel(window.getProgrammeLevel());
        dto.setSession(window.getSession());
        dto.setStartDate(window.getStartDate());
        dto.setEndDate(window.getEndDate());
        dto.setProgrammeIds(window.getAdmissionWindowProgrammes().stream()
                .map(awp -> awp.getProgramme().getProgrammeId()).collect(Collectors.toList()));
        return dto;
    }

    // CHANGED: Short id to String admissionCode
    @Override
    @Transactional
    public void deleteAdmissionWindow(String admissionCode) {
        AdmissionWindow window = findByCode(admissionCode);
        admissionWindowRepository.delete(window);
    }

    // CHANGED: Short id to String admissionCode
    @Override
    @Transactional
    public AdmissionWindow toggleIsActive(String admissionCode) {
        AdmissionWindow w = findByCode(admissionCode);
        w.setActive(!w.isActive());
        return admissionWindowRepository.save(w);
    }

    @Override
    public List<AdmissionWindow> getLatestAdmissionWindows() {
        return admissionWindowRepository.findTop5ByOrderByAdmissionIdDesc();
    }

    // CHANGED: Short admissionId to String admissionCode
    @Override
    public List<AdmissionWindowProgrammeResponseDTO> getProgrammesForWindow(String admissionCode) {
        AdmissionWindow window = findByCode(admissionCode);
        return window.getAdmissionWindowProgrammes().stream()
                .map(awc -> new AdmissionWindowProgrammeResponseDTO(awc.getId(), awc.getProgramme().getProgrammeName(),
                        awc.isActive()))
                .sorted(Comparator.comparing(AdmissionWindowProgrammeResponseDTO::getProgrammeName))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean removeProgrammeFromWindow(Short admissionWindowProgrammeId) {
        AdmissionWindowProgramme association = admissionWindowProgrammeRepository.findById(admissionWindowProgrammeId)
                .orElseThrow(() -> new EntityNotFoundException("Link not found"));
        AdmissionWindow parent = association.getAdmissionWindow();
        if (parent != null && parent.getAdmissionWindowProgrammes().size() <= 1) {
            admissionWindowRepository.delete(parent);
            return true;
        } else {
            admissionWindowProgrammeRepository.delete(association);
            return false;
        }
    }

    @Override
    public void toggleProgrammeStatusInWindow(Short admissionWindowProgrammeId) {
        AdmissionWindowProgramme awc = admissionWindowProgrammeRepository.findById(admissionWindowProgrammeId)
                .orElseThrow(() -> new EntityNotFoundException("Link not found"));
        awc.setActive(!awc.isActive());
        admissionWindowProgrammeRepository.save(awc);
    }

    // CHANGED: Short excludeId to String excludeWindowCode
    @Override
    public String getExistingWindowSession(Short streamId, ProgrammeLevel level, String excludeWindowCode) {
        Stream s = streamRepository.findById(streamId)
                .orElseThrow(() -> new EntityNotFoundException("Stream not found"));
        List<AdmissionWindow> existing = admissionWindowRepository.findByStreamAndProgrammeLevel(s, level);

        if (excludeWindowCode != null) {
            // Filter out the one we are editing
            existing = existing.stream().filter(w -> !w.getAdmissionCode().equals(excludeWindowCode))
                    .collect(Collectors.toList());
        }
        return existing.isEmpty() ? null : existing.get(0).getSession();
    }

    @Override
    public boolean isDuplicateWindow(Short streamId, ProgrammeLevel level, String session, String excludeWindowCode) {

        Stream stream = null;

        if (streamId != null) {
            stream = streamRepository.findById(streamId)
                    .orElseThrow(() -> new EntityNotFoundException("Stream not found"));
        }

        List<AdmissionWindow> existing;

        if (stream == null) {
            // ALL STREAMS
            existing = admissionWindowRepository
                    .findByStreamIsNullAndProgrammeLevelAndSessionAndIsActiveTrueAndAdmissionCodeNot(level, session,
                            excludeWindowCode);
        } else {
            // Specific stream
            existing = admissionWindowRepository
                    .findByStreamAndProgrammeLevelAndSessionAndIsActiveTrueAndAdmissionCodeNot(stream, level, session,
                            excludeWindowCode);

            // 🔥 Optional but recommended: prevent conflict with ALL STREAMS
            List<AdmissionWindow> allStream = admissionWindowRepository
                    .findByStreamIsNullAndProgrammeLevelAndSessionAndIsActiveTrueAndAdmissionCodeNot(level, session,
                            excludeWindowCode);

            if (!allStream.isEmpty()) {
                return true; // duplicate because ALL STREAMS exists
            }
        }

        return !existing.isEmpty();
    }

}