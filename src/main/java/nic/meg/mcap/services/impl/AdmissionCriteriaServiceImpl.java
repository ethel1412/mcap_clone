package nic.meg.mcap.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import nic.meg.mcap.dto.request.AdmissionCriteriaRequestDTO;
import nic.meg.mcap.dto.request.TieBreakerCriterionDTO;
import nic.meg.mcap.dto.response.AdmissionCriteriaResponseDTO;
import nic.meg.mcap.dto.response.ProgrammeWithCriteriaDTO;
import nic.meg.mcap.entities.AdmissionCriteria;
import nic.meg.mcap.entities.AdmissionWindow;
import nic.meg.mcap.entities.Programme;
import nic.meg.mcap.entities.ProgrammeOffered;
import nic.meg.mcap.enums.ProgrammeLevel;
import nic.meg.mcap.repositories.AdmissionCriteriaRepository;
import nic.meg.mcap.repositories.AdmissionWindowRepository;
import nic.meg.mcap.repositories.EligibilityCriteriaRepository;
import nic.meg.mcap.repositories.ProgrammeRepository;
import nic.meg.mcap.repositories.ProgrammesOfferedRepository;
import nic.meg.mcap.repositories.StreamRepository;
import nic.meg.mcap.services.AdmissionCriteriaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class AdmissionCriteriaServiceImpl implements AdmissionCriteriaService {

    private final AdmissionCriteriaRepository criteriaRepository;
    private final AdmissionWindowRepository admissionWindowRepository;
    private final StreamRepository streamRepository; // kept (you said don't remove stream yet)
    private final ProgrammeRepository programmeRepository;
    private final ProgrammesOfferedRepository programmesOfferedRepository;
    private final EligibilityCriteriaRepository eligibilityCriteriaRepository; // will be used later for validation if needed
    private final ObjectMapper objectMapper;

    @Override
    public AdmissionCriteriaResponseDTO saveOrUpdateCriteria(AdmissionCriteriaRequestDTO dto) {

        AdmissionWindow admissionWindow = admissionWindowRepository.findById(dto.getAdmissionWindowId())
                .orElseThrow(() -> new EntityNotFoundException("Admission window not found"));

        // Programme is required (UG + PG)
        Programme programme = programmeRepository.findById(dto.getProgrammeId())
                .orElseThrow(() -> new EntityNotFoundException("Programme not found"));

        // Find existing (programme-wise)
        Optional<AdmissionCriteria> existing =
                criteriaRepository.findByAdmissionWindowAdmissionIdAndProgrammeProgrammeId(
                        dto.getAdmissionWindowId(), dto.getProgrammeId());

        AdmissionCriteria criteria = existing.orElseGet(AdmissionCriteria::new);

        // Common fields
        criteria.setAdmissionWindow(admissionWindow);
        criteria.setProgramme(programme);
        criteria.setProgrammeLevel(dto.getProgrammeLevel());
        criteria.setActive(true);

        // Keep stream field for compatibility (optional)
        // If you don't want to persist stream anymore, send streamId as null from UI.
        if (dto.getStreamId() != null) {
            criteria.setStream(streamRepository.findById(dto.getStreamId())
                    .orElseThrow(() -> new EntityNotFoundException("Stream not found")));
        } else {
            criteria.setStream(null);
        }

        // Save CUET merit subjects (JSON)
        try {
            if (dto.getCuetMeritSubjects() != null && !dto.getCuetMeritSubjects().isEmpty()) {
                criteria.setCuetMeritSubjectsJson(objectMapper.writeValueAsString(dto.getCuetMeritSubjects()));
            } else {
                criteria.setCuetMeritSubjectsJson(null);
            }
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid CUET merit subjects: " + e.getMessage(), e);
        }

        // Save Non-CUET merit subjects (JSON)
        try {
            if (dto.getNonCuetMeritSubjects() != null && !dto.getNonCuetMeritSubjects().isEmpty()) {
                criteria.setNonCuetMeritSubjectsJson(objectMapper.writeValueAsString(dto.getNonCuetMeritSubjects()));
            } else {
                criteria.setNonCuetMeritSubjectsJson(null);
            }
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid non-CUET merit subjects: " + e.getMessage(), e);
        }

        // Save tie-breaker config (JSON) - existing behavior
        try {
            if (dto.getTiebreakerConfig() != null && !dto.getTiebreakerConfig().isEmpty()) {
                criteria.setTiebreakerConfig(objectMapper.writeValueAsString(dto.getTiebreakerConfig()));
            } else {
                criteria.setTiebreakerConfig(null);
            }
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid tiebreaker config: " + e.getMessage(), e);
        }

        criteria = criteriaRepository.save(criteria);
        return convertToDTO(criteria);
    }

    @Override
    public AdmissionCriteriaResponseDTO getCriteriaForUG(Short admissionWindowId, Short programmeId) {
        AdmissionCriteria criteria = criteriaRepository
                .findByAdmissionWindowAdmissionIdAndProgrammeProgrammeId(admissionWindowId, programmeId)
                .orElse(null);
        return criteria != null ? convertToDTO(criteria) : null;
    }

    @Override
    public AdmissionCriteriaResponseDTO getCriteriaForPG(Short admissionWindowId, Short programmeId) {
        AdmissionCriteria criteria = criteriaRepository
                .findByAdmissionWindowAdmissionIdAndProgrammeProgrammeId(admissionWindowId, programmeId)
                .orElse(null);
        return criteria != null ? convertToDTO(criteria) : null;
    }

    @Override
    public List<ProgrammeWithCriteriaDTO> getProgrammesWithCriteriaStatus(Short admissionWindowId) {
        AdmissionWindow window = admissionWindowRepository.findById(admissionWindowId)
                .orElseThrow(() -> new EntityNotFoundException("Admission window not found"));

        Short streamId = window.getStream() != null ? window.getStream().getStreamId() : null;
        ProgrammeLevel level = window.getProgrammeLevel();

        List<Programme> programmes = programmeRepository.findByStreamStreamIdAndProgrammeLevel(streamId, level);

        List<ProgrammeWithCriteriaDTO> result = new ArrayList<>();
        for (Programme p : programmes) {
            AdmissionCriteria criteria = criteriaRepository
                    .findByAdmissionWindowAdmissionIdAndProgrammeProgrammeId(admissionWindowId, p.getProgrammeId())
                    .orElse(null);

            ProgrammeWithCriteriaDTO dto = ProgrammeWithCriteriaDTO.builder()
                    .programmeId(p.getProgrammeId())
                    .programmeName(p.getProgrammeName())
                    .programmeLevel(p.getProgrammeLevel())
                    .streamName(p.getStream().getStreamName())
                    .hasCriteria(criteria != null)
                    .criteria(criteria != null ? convertToDTO(criteria) : null)
                    .build();

            result.add(dto);
        }
        return result;
    }

    @Override
    public List<ProgrammeWithCriteriaDTO> getProgrammeOfferedWithCriteriaStatus(Short admissionWindowId) {
        AdmissionWindow window = admissionWindowRepository.findById(admissionWindowId)
                .orElseThrow(() -> new EntityNotFoundException("Admission window not found"));

        List<Short> streamIds;

        if (window.getStream() != null && window.getStream().getStreamId() != null) {
            streamIds = List.of(window.getStream().getStreamId());
        } else {
            streamIds = List.of((short) 101, (short) 102, (short) 103);
        }

        List<ProgrammeOffered> programmeOfferedList =
                programmesOfferedRepository.findByStreamProgramme(streamIds, window.getProgrammeLevel());

        List<ProgrammeWithCriteriaDTO> mapped = programmeOfferedList.stream().map(po -> {
            ProgrammeWithCriteriaDTO dto = new ProgrammeWithCriteriaDTO();
            dto.setProgrammeId(po.getProgramme().getProgrammeId());
            dto.setProgrammeName(po.getProgramme().getProgrammeName());
            dto.setStreamName(po.getProgramme().getStream().getStreamName());
            dto.setProgrammeLevel(po.getProgramme().getProgrammeLevel());
            dto.setHasCriteria(criteriaRepository.existsByAdmissionWindowAdmissionIdAndProgrammeProgrammeId(
                    admissionWindowId, po.getProgramme().getProgrammeId()));
            return dto;
        }).collect(Collectors.toList());

        // de-duplicate by programmeId
        return mapped.stream()
                .collect(Collectors.toMap(
                        ProgrammeWithCriteriaDTO::getProgrammeId,
                        dto -> dto,
                        (existing, duplicate) -> existing
                ))
                .values()
                .stream()
                .toList();
    }

    @Override
    public boolean hasCriteria(Short admissionWindowId, Short streamId, Short programmeId) {
        if (programmeId != null) {
            return criteriaRepository.existsByAdmissionWindowAdmissionIdAndProgrammeProgrammeId(admissionWindowId, programmeId);
        }
        if (streamId != null) {
            return criteriaRepository.existsByAdmissionWindowAdmissionIdAndStreamStreamId(admissionWindowId, streamId);
        }
        return false;
    }

    @Override
    public void deleteCriteria(Long criteriaId) {
        criteriaRepository.deleteById(criteriaId);
    }

    // -----------------------------------------------------------------------
    // DTO conversion
    // -----------------------------------------------------------------------

    private AdmissionCriteriaResponseDTO convertToDTO(AdmissionCriteria entity) {

        String windowName = (entity.getAdmissionWindow().getStream() != null
                ? entity.getAdmissionWindow().getStream().getStreamName() : "All Streams") + " ("
                + entity.getAdmissionWindow().getProgrammeLevel() + ") - "
                + entity.getAdmissionWindow().getSession();

        AdmissionCriteriaResponseDTO.AdmissionCriteriaResponseDTOBuilder builder = AdmissionCriteriaResponseDTO.builder()
                .criteriaId(entity.getCriteriaId())
                .admissionWindowId(entity.getAdmissionWindow().getAdmissionId())
                .admissionWindowName(windowName)
                .streamId(entity.getStream() != null ? entity.getStream().getStreamId() : null)
                .streamName(entity.getStream() != null ? entity.getStream().getStreamName() : null)
                .programmeId(entity.getProgramme() != null ? entity.getProgramme().getProgrammeId() : null)
                .programmeName(entity.getProgramme() != null ? entity.getProgramme().getProgrammeName() : null)
                .programmeLevel(entity.getProgrammeLevel())
                .isActive(entity.isActive());

        // Parse CUET merit subjects JSON
        try {
            if (entity.getCuetMeritSubjectsJson() != null && !entity.getCuetMeritSubjectsJson().isBlank()) {
                builder.cuetMeritSubjects(objectMapper.readValue(
                        entity.getCuetMeritSubjectsJson(),
                        new TypeReference<List<String>>() {
                        }
                ));
            }
        } catch (Exception e) {
            builder.cuetMeritSubjects(new ArrayList<>());
        }

        // Parse non-CUET merit subjects JSON
        try {
            if (entity.getNonCuetMeritSubjectsJson() != null && !entity.getNonCuetMeritSubjectsJson().isBlank()) {
                builder.nonCuetMeritSubjects(objectMapper.readValue(
                        entity.getNonCuetMeritSubjectsJson(),
                        new TypeReference<List<String>>() {
                        }
                ));
            }
        } catch (Exception e) {
            builder.nonCuetMeritSubjects(new ArrayList<>());
        }

        // Parse tie-breaker config JSON (existing)
        try {
            if (entity.getTiebreakerConfig() != null && !entity.getTiebreakerConfig().isBlank()) {
                builder.tiebreakerConfig(objectMapper.readValue(
                        entity.getTiebreakerConfig(),
                        new TypeReference<List<TieBreakerCriterionDTO>>() {
                        }
                ));
            }
        } catch (Exception e) {
            builder.tiebreakerConfig(new ArrayList<>());
        }

        return builder.build();
    }
}