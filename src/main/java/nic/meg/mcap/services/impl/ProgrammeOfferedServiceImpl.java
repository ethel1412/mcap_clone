package nic.meg.mcap.services.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import nic.meg.mcap.dto.request.ProgrammeOfferedBatchAssignRequestDTO;
import nic.meg.mcap.dto.request.ProgrammeOfferedRequestDTO;
import nic.meg.mcap.dto.response.ProgrammeOfferedResponseDTO;
import nic.meg.mcap.entities.AdmissionWindow;
import nic.meg.mcap.entities.AdmissionWindowProgramme;
import nic.meg.mcap.entities.InstituteDepartment;
import nic.meg.mcap.entities.Programme;
import nic.meg.mcap.entities.ProgrammeOffered;
import nic.meg.mcap.enums.ProgrammeLevel;
import nic.meg.mcap.enums.Shift;
import nic.meg.mcap.enums.InstituteStatus;
import nic.meg.mcap.repositories.AdmissionWindowProgrammeRepository;
import nic.meg.mcap.repositories.AdmissionWindowRepository;
import nic.meg.mcap.repositories.InstituteDepartmentRepository;
import nic.meg.mcap.repositories.ProgrammeRepository;
import nic.meg.mcap.repositories.ProgrammesOfferedRepository;
import nic.meg.mcap.services.ProgrammeOfferedService;

@Service
public class ProgrammeOfferedServiceImpl implements ProgrammeOfferedService {

    @Autowired
    private ProgrammesOfferedRepository programmesOfferedRepository;

    @Autowired
    private InstituteDepartmentRepository instituteDepartmentRepository;

    @Autowired
    private ProgrammeRepository programmeRepository;

    @Autowired
    private AdmissionWindowRepository admissionWindowRepository;

    @Autowired
    private AdmissionWindowProgrammeRepository admissionWindowProgrammeRepository;

    @Override
    @Transactional
    public List<ProgrammeOfferedResponseDTO> createProgrammeOffered(ProgrammeOfferedRequestDTO requestDTO,
                                                                    Short loggedInInstituteId) {

        InstituteDepartment instituteDepartment = instituteDepartmentRepository
                .findById(requestDTO.getInstituteDepartmentId()).orElseThrow(() -> new EntityNotFoundException(
                        "InstituteDepartment not found with ID: " + requestDTO.getInstituteDepartmentId()));

        if (!instituteDepartment.getInstitute().getInstituteId().equals(loggedInInstituteId)) {
            throw new SecurityException("Unauthorized: You cannot add a programme to another institute.");
        }

        Short instituteId = instituteDepartment.getInstitute().getInstituteId();

        List<ProgrammeOffered> existingProgrammes = programmesOfferedRepository
                .findWithDetailsByInstituteDepartment_Institute_InstituteId(instituteId);

        List<ProgrammeOfferedResponseDTO> responses = new ArrayList<>();

        for (Short programmeId : requestDTO.getProgrammeIds()) {
            Programme programme = programmeRepository.findById(programmeId)
                    .orElseThrow(() -> new EntityNotFoundException("Programme not found with ID: " + programmeId));

            for (Shift shift : requestDTO.getShift()) {

                Shift requestShift = (shift != null) ? shift : Shift.NA;

                Optional<ProgrammeOffered> duplicate = existingProgrammes.stream()
                        .filter(co -> co.getProgramme() != null
                                && co.getProgramme().getProgrammeId().equals(programme.getProgrammeId())
                                && co.getShift() == requestShift)
                        .findFirst();

                if (duplicate.isPresent()) {
                    String existingDept = duplicate.get().getInstituteDepartment().getDepartment().getDepartmentName();

                    throw new IllegalStateException("Programme " + programme.getProgrammeName() + " already exists in "
                            + requestShift.getDisplayName() + " shift under: " + existingDept);
                }

                ProgrammeOffered po = new ProgrammeOffered();
                po.setInstituteDepartment(instituteDepartment);
                po.setProgramme(programme);
                po.setShift(requestShift);

                ProgrammeOffered saved = programmesOfferedRepository.save(po);
                responses.add(convertToDTO(saved));
            }
        }

        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProgrammeOfferedResponseDTO> getProgrammeOfferedById(Integer id) {

        ProgrammeOffered existing = programmesOfferedRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ProgrammeOffered not found with ID: " + id));

        Programme programme = existing.getProgramme();
        InstituteDepartment instituteDepartment = existing.getInstituteDepartment();

        List<ProgrammeOffered> list = programmesOfferedRepository.findByProgrammeAndInstituteDepartment(programme,
                instituteDepartment);

        return list.stream().map(this::convertToDTO).toList();
    }

    @Override
    @Transactional
    public ProgrammeOfferedResponseDTO updateProgrammeOffered(Integer id, ProgrammeOfferedRequestDTO requestDTO,
                                                              Short loggedInInstituteId) {

        ProgrammeOffered existing = programmesOfferedRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ProgrammeOffered not found with ID: " + id));

        if (!existing.getInstituteDepartment().getInstitute().getInstituteId().equals(loggedInInstituteId)) {
            throw new SecurityException("Unauthorized: You do not have permission to modify this programme.");
        }

        InstituteDepartment instituteDepartment = instituteDepartmentRepository
                .findById(requestDTO.getInstituteDepartmentId()).orElseThrow(() -> new EntityNotFoundException(
                        "InstituteDepartment not found with ID: " + requestDTO.getInstituteDepartmentId()));

        if (!instituteDepartment.getInstitute().getInstituteId().equals(loggedInInstituteId)) {
            throw new SecurityException("Unauthorized: You cannot assign to another institute.");
        }

        if (requestDTO.getProgrammeIds() == null || requestDTO.getProgrammeIds().isEmpty()) {
            throw new IllegalArgumentException("Programme ID is required for update");
        }

        Short programmeId = requestDTO.getProgrammeIds().get(0).shortValue();

        Programme programme = programmeRepository.findById(programmeId)
                .orElseThrow(() -> new EntityNotFoundException("Programme not found with ID: " + programmeId));

        List<ProgrammeOffered> existingRows = programmesOfferedRepository
                .findByProgrammeAndInstituteDepartment(programme, instituteDepartment);

        Set<Shift> existingShifts = existingRows.stream().map(ProgrammeOffered::getShift).collect(Collectors.toSet());

        Set<Shift> newShifts = requestDTO.getShift() != null ? new HashSet<>(requestDTO.getShift()) : new HashSet<>();

        for (Shift shift : newShifts) {
            if (!existingShifts.contains(shift)) {
                ProgrammeOffered newEntry = new ProgrammeOffered();
                newEntry.setProgramme(programme);
                newEntry.setInstituteDepartment(instituteDepartment);
                newEntry.setShift(shift);

                programmesOfferedRepository.save(newEntry);
            }
        }

        for (ProgrammeOffered row : existingRows) {
            if (!newShifts.contains(row.getShift())) {
                programmesOfferedRepository.delete(row);
            }
        }

        ProgrammeOffered updated = programmesOfferedRepository
                .findByProgrammeAndInstituteDepartment(programme, instituteDepartment).stream().findFirst()
                .orElseThrow();

        return convertToDTO(updated);
    }

    @Override
    @Transactional
    public void deleteProgrammeOffered(Integer id, Short loggedInInstituteId) {
        ProgrammeOffered existing = programmesOfferedRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ProgrammeOffered not found with ID: " + id));

        if (!existing.getInstituteDepartment().getInstitute().getInstituteId().equals(loggedInInstituteId)) {
            throw new SecurityException("Unauthorized: You do not have permission to delete this programme.");
        }

        programmesOfferedRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProgrammeOfferedResponseDTO> listProgrammesByInstituteDepartment(Short instituteId,
                                                                                 Short departmentId) {
        List<ProgrammeOffered> programmeOffereds = programmesOfferedRepository
                .findWithDetailsByInstituteDepartment_Institute_InstituteId(instituteId);

        List<ProgrammeOfferedResponseDTO> result = new ArrayList<>();
        for (ProgrammeOffered co : programmeOffereds) {
            if (co.getInstituteDepartment() != null && co.getInstituteDepartment().getDepartment() != null
                    && co.getInstituteDepartment().getDepartment().getDepartmentId().equals(departmentId)) {
                result.add(convertToDTO(co));
            }
        }
        return result;
    }

    public Map<String, Map<String, Map<String, List<ProgrammeOfferedResponseDTO>>>> getGroupedData() {

        List<ProgrammeOfferedResponseDTO> list = getAllProgrammesOffered();

        return list.stream().collect(
                Collectors.groupingBy(
                        ProgrammeOfferedResponseDTO::getInstituteName,
                        LinkedHashMap::new,
                        Collectors.groupingBy(
                                ProgrammeOfferedResponseDTO::getDepartmentName,
                                LinkedHashMap::new,
                                Collectors.groupingBy(
                                        ProgrammeOfferedResponseDTO::getProgrammeName,
                                        LinkedHashMap::new,
                                        Collectors.toList()
                                )
                        )
                )
        );
    }
    @Override
    @Transactional
    public void assignMultipleProgrammesToDepartment(ProgrammeOfferedBatchAssignRequestDTO batchRequest,
                                                     Short loggedInInstituteId) {
        InstituteDepartment instituteDepartment = instituteDepartmentRepository
                .findById(batchRequest.getInstituteDepartmentId()).orElseThrow(() -> new EntityNotFoundException(
                        "InstituteDepartment not found with ID: " + batchRequest.getInstituteDepartmentId()));

        if (!instituteDepartment.getInstitute().getInstituteId().equals(loggedInInstituteId)) {
            throw new SecurityException(
                    "Unauthorized: You cannot assign programmes to a department belonging to another institute.");
        }

        Short instituteId = instituteDepartment.getInstitute().getInstituteId();
        List<ProgrammeOffered> existingOfferedProgrammes = programmesOfferedRepository
                .findWithDetailsByInstituteDepartment_Institute_InstituteId(instituteId);
        List<ProgrammeOffered> newProgrammesOffered = new ArrayList<>();

        Shift batchShift = Shift.NA;

        for (Short ProgrammeId : batchRequest.getProgrammeIds()) {
            Programme programme = programmeRepository.findById(ProgrammeId)
                    .orElseThrow(() -> new EntityNotFoundException("Programme not found with ID: " + ProgrammeId));

            boolean exists = existingOfferedProgrammes.stream()
                    .anyMatch(co -> co.getProgramme() != null
                            && co.getProgramme().getProgrammeId().equals(programme.getProgrammeId())
                            && co.getShift() == batchShift);

            if (!exists) {
                ProgrammeOffered co = new ProgrammeOffered();
                co.setInstituteDepartment(instituteDepartment);
                co.setProgramme(programme);
                co.setShift(batchShift);
                newProgrammesOffered.add(co);
            }
        }
        programmesOfferedRepository.saveAll(newProgrammesOffered);
    }

    private ProgrammeOfferedResponseDTO convertToDTO(ProgrammeOffered entity) {
        ProgrammeOfferedResponseDTO dto = new ProgrammeOfferedResponseDTO();
        dto.setProgrammeOfferedId(entity.getProgrammeOfferedId());

        if (entity.getInstituteDepartment() != null) {
            dto.setInstituteDepartmentId(entity.getInstituteDepartment().getInstituteDepartmentId());
            if (entity.getInstituteDepartment().getInstitute() != null) {
                dto.setInstituteId(entity.getInstituteDepartment().getInstitute().getInstituteId());
                dto.setInstituteName(entity.getInstituteDepartment().getInstitute().getInstituteName());
                dto.setUniversityName(
                        entity.getInstituteDepartment().getInstitute().getUniversityName()
                );
                dto.setProspectusUrl(entity.getInstituteDepartment().getInstitute().getProspectusUrl());
            }
            if (entity.getInstituteDepartment().getDepartment() != null) {
                dto.setDepartmentId(entity.getInstituteDepartment().getDepartment().getDepartmentId());
                dto.setDepartmentName(entity.getInstituteDepartment().getDepartment().getDepartmentName());
            }
        }

        if (entity.getProgramme() != null) {
            dto.setProgrammeId(entity.getProgramme().getProgrammeId());
            dto.setProgrammeName(entity.getProgramme().getProgrammeName());
            dto.setProgrammeLevel(
                    entity.getProgramme().getProgrammeLevel() != null ? entity.getProgramme().getProgrammeLevel().name()
                            : null);

            if (entity.getProgramme().getStream() != null) {
                dto.setStreamId(entity.getProgramme().getStream().getStreamId());
                dto.setStreamName(entity.getProgramme().getStream().getStreamName());
            }
        }

        if (entity.getShift() != null) {
            dto.setShift(entity.getShift());
            dto.setShiftDisplayName(entity.getShift().getDisplayName());
        } else {
            dto.setShift(Shift.NA);
            dto.setShiftDisplayName(Shift.NA.getDisplayName());
        }

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProgrammeOfferedResponseDTO> getAllProgrammesOffered() {
        // Only surface programmes from institutes that are ACCEPTED and active.
        // This drives the public participating-institutes page and the applicant
        // programme-preference page.
        return programmesOfferedRepository.findAllByActiveAndAcceptedInstitutes()
                .stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProgrammeOfferedResponseDTO> getProgrammesOfferedByProgrammeName(String ProgrammeName) {
        return programmesOfferedRepository.findByProgramme_ProgrammeName(ProgrammeName).stream().map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProgrammeOfferedResponseDTO> listProgrammesByInstitute(Short instituteId) {
        return programmesOfferedRepository.findWithDetailsByInstituteDepartment_Institute_InstituteId(instituteId).stream().map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProgrammeOfferedResponseDTO> getProgrammesOfferedByInstituteAndStream(Short instituteId,
                                                                                      Short streamId) {
        return programmesOfferedRepository.findWithDetailsByInstituteDepartment_Institute_InstituteId(instituteId).stream()
                .filter(co -> co.getProgramme() != null && co.getProgramme().getStream() != null
                        && co.getProgramme().getStream().getStreamId().equals(streamId))
                .map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProgrammeOffered findByIdAndInstituteUsername(Integer id, String username) {
        return programmesOfferedRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Programme offered not found"));
    }

    @Override
    public Optional<ProgrammeOffered> findById(Integer programmeOfferedId) {
        return programmesOfferedRepository.findById(programmeOfferedId);
    }

    @Override
    public List<String> findDistinctProgrammeLevelsByInstitute(Short instituteId) {
        return programmesOfferedRepository.findDistinctProgrammeLevelsByInstitute(instituteId);
    }

    @Override
    public List<ProgrammeOfferedResponseDTO> findProgrammesByLevelAndInstitute(ProgrammeLevel level,
                                                                               Short instituteId) {
        return programmesOfferedRepository.findByProgrammeLevelAndInstitute(level, instituteId).stream()
                .map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public List<ProgrammeOfferedResponseDTO> listProgrammesByInstituteAndProgrammeIds(Short instituteId,
                                                                                      Collection<Short> programmeIds) {
        if (programmeIds == null || programmeIds.isEmpty())
            return List.of();
        Set<Short> idSet = new HashSet<>(programmeIds);
        return listProgrammesByInstitute(instituteId).stream().filter(po -> idSet.contains(po.getProgrammeId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<ProgrammeOffered> getProgrammesForAdmissionWindow(Short admissionWindowId) {

        AdmissionWindow window = admissionWindowRepository.findById(admissionWindowId)
                .orElseThrow(() -> new EntityNotFoundException("Window not found"));

        List<AdmissionWindowProgramme> mappings = admissionWindowProgrammeRepository
                .findByAdmissionWindowAndIsActiveTrue(window);

        if (!mappings.isEmpty()) {

            Set<Short> programmeIds = mappings.stream().map(m -> m.getProgramme().getProgrammeId())
                    .collect(Collectors.toSet());

            return programmesOfferedRepository.findByProgramme_ProgrammeIdIn(programmeIds);
        }

        if (window.getStream() != null) {
            return programmesOfferedRepository.findByLevelAndStreamId(window.getProgrammeLevel(),
                    window.getStream().getStreamId());
        }

        return programmesOfferedRepository.findByProgramme_ProgrammeLevel(window.getProgrammeLevel()).stream().toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProgrammeOfferedResponseDTO> findInstitutesByProgramme(Short programmeId) {
        List<ProgrammeOffered> offered = programmesOfferedRepository.findByProgrammeProgrammeId(programmeId, InstituteStatus.ACCEPTED);
        return offered.stream().map(this::convertToDTO)
                .collect(Collectors.toMap(ProgrammeOfferedResponseDTO::getInstituteId, dto -> dto, (a, b) -> a))
                .values().stream().collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Long countByInstitute(Short instituteId) {
        Long count = programmesOfferedRepository.countByInstituteDepartment_Institute_InstituteId(instituteId);
        return count != null ? count : 0L;
    }

    @Override
    @Transactional
    public void deleteAllShifts(Integer programmeOfferedId, Short instituteId) {

        ProgrammeOffered po = programmesOfferedRepository.findById(programmeOfferedId)
                .orElseThrow(() -> new EntityNotFoundException("ProgrammeOffered not found"));

        if (!po.getInstituteDepartment().getInstitute().getInstituteId().equals(instituteId)) {
            throw new SecurityException("Unauthorized access");
        }

        Short programmeId = po.getProgramme().getProgrammeId();
        Integer instituteDeptId = po.getInstituteDepartment().getInstituteDepartmentId();

        int deleted = programmesOfferedRepository
                .deleteByProgramme_ProgrammeIdAndInstituteDepartment_InstituteDepartmentId(programmeId,
                        instituteDeptId);

        if (deleted == 0) {
            throw new RuntimeException("No shifts found to delete");
        }
    }
}