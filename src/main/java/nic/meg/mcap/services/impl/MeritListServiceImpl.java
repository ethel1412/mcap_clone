package nic.meg.mcap.services.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nic.meg.mcap.dto.response.ApplicantCountDTO;
import nic.meg.mcap.dto.response.MeritListMetadataDTO;
import nic.meg.mcap.dto.response.MeritListResponseDTO;
import nic.meg.mcap.dto.response.PagedResponse;
import nic.meg.mcap.entities.AdmissionWindow;
import nic.meg.mcap.entities.Application;
import nic.meg.mcap.entities.MeritList;
import nic.meg.mcap.entities.MeritListEntry;
import nic.meg.mcap.entities.Schedule;
import nic.meg.mcap.enums.AllotmentStatus;
import nic.meg.mcap.enums.ApplicantType;
import nic.meg.mcap.repositories.AdmissionWindowRepository;
import nic.meg.mcap.repositories.ApplicationRepository;
import nic.meg.mcap.repositories.MeritListEntryRepository;
import nic.meg.mcap.repositories.MeritListRepository;
import nic.meg.mcap.repositories.ProgrammeRepository;
import nic.meg.mcap.repositories.SeatAllotmentRepository;
import nic.meg.mcap.repositories.ScheduleRepository;
import nic.meg.mcap.services.MeritListService;
import nic.meg.mcap.services.impl.merit.MeritListGenerator;
import nic.meg.mcap.services.impl.merit.MeritListMapper;
import nic.meg.mcap.services.impl.merit.MeritListPersistenceService;
import nic.meg.mcap.services.impl.merit.RoundPhaseNormalizer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import nic.meg.mcap.dto.response.MeritListRowDTO;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class MeritListServiceImpl implements MeritListService {

    private static final String DEFAULT_ROUND_TYPE = "CUET";
    private static final Integer DEFAULT_PHASE_NO = 1;
    private static final String STATUS_PUBLISHED = "PUBLISHED";

    private final MeritListRepository meritListRepository;
    private final AdmissionWindowRepository admissionWindowRepository;
    private final ProgrammeRepository programmeRepository;
    private final ApplicationRepository applicationRepository;
    private final SeatAllotmentRepository seatAllotmentRepository;
    private final MeritListEntryRepository entryRepository;
    private final MeritListGenerator meritListGenerator;
    private final MeritListMapper meritListMapper;
    private final MeritListPersistenceService persistenceService;
    private final RoundPhaseNormalizer roundPhaseNormalizer;
    private final ScheduleRepository scheduleRepository;

    @Transactional(readOnly = true)
    @Override
    public MeritListResponseDTO getLatestMeritListByProgramme(
            String admissionWindowCode,
            Short programmeId,
            String roundType,
            Integer phaseNo) {

        AdmissionWindow window = admissionWindowRepository.findByAdmissionCode(admissionWindowCode)
                .orElseThrow(() -> new EntityNotFoundException("Admission window not found: " + admissionWindowCode));
        Short admissionWindowId = window.getAdmissionId();

        String rt = roundPhaseNormalizer.normalizeRoundType(roundType);
        Integer ph = roundPhaseNormalizer.normalizePhaseNo(phaseNo);

        String mappedApplicantType = "WITH_ENTRANCE"; // Default to CUET/Entrance
        if ("NONCUET".equalsIgnoreCase(rt) || "NON_CUET".equalsIgnoreCase(rt)) {
            mappedApplicantType = "WITHOUT_ENTRANCE";
        }

        MeritList meritList = meritListRepository
                .findByAdmissionWindowAndProgrammeAndApplicantTypeAndRoundAndPhase(
                        admissionWindowId,
                        programmeId,
                        mappedApplicantType,
                        rt,
                        ph)
                .orElseThrow(() -> new EntityNotFoundException("Merit list not found for this specific programme and round type"));

        List<MeritListEntry> entries = entryRepository.findByMeritListOrderByRank(meritList.getMeritListId());

        return MeritListResponseDTO.builder()
                .metadata(meritListMapper.toMetadataDTO(meritList))
                .entries(entries.stream()
                        .map(meritListMapper::toRowDTO)
                        .collect(Collectors.toList()))
                .build();
    }

    @Override
    public MeritListMetadataDTO generateUGMeritList(String admissionWindowCode, Short programmeId, String roundType, Integer phaseNo) {
        return generateMeritListInternal(admissionWindowCode, programmeId, roundType, phaseNo, true);
    }

    @Override
    public MeritListMetadataDTO generatePGMeritList(String admissionWindowCode, Short programmeId, String roundType, Integer phaseNo) {
        return generateMeritListInternal(admissionWindowCode, programmeId, roundType, phaseNo, false);
    }

    @Override
    public MeritListMetadataDTO generateUGMeritList(String admissionWindowCode, Short programmeId) {
        return generateMeritListInternal(admissionWindowCode, programmeId, DEFAULT_ROUND_TYPE, DEFAULT_PHASE_NO, true);
    }

    @Override
    public MeritListMetadataDTO generatePGMeritList(String admissionWindowCode, Short programmeId) {
        return generateMeritListInternal(admissionWindowCode, programmeId, DEFAULT_ROUND_TYPE, DEFAULT_PHASE_NO, false);
    }

    private MeritListMetadataDTO generateMeritListInternal(String admissionWindowCode, Short programmeId, String frontendRoundType, Integer phaseNo, boolean isUG) {

        AdmissionWindow window = admissionWindowRepository.findByAdmissionCode(admissionWindowCode)
                .orElseThrow(() -> new EntityNotFoundException("Admission window not found: " + admissionWindowCode));
        Short admissionWindowId = window.getAdmissionId();

        Integer phNorm = roundPhaseNormalizer.normalizePhaseNo(phaseNo);

        // =====================================================================
        // ROUTE PROTECTION LOGIC
        // If the frontend asks for a phase that is actually "COMBINED" in the db,
        // we run both CUET and Non-CUET engines.
        // =====================================================================
        List<Schedule> allSchedules = scheduleRepository.findByAdmissionWindowIdOrderByStepOrder(admissionWindowId);
        String actualRoute = frontendRoundType;
        for (Schedule s : allSchedules) {
            if ("COUNSELLING".equals(s.getCategory()) && s.getPhaseNumber() != null && s.getPhaseNumber().equals(phNorm)) {
                if ("COMBINED".equalsIgnoreCase(s.getAdmissionRoute())) {
                    actualRoute = "COMBINED";
                    break;
                } else if (actualRoute.equalsIgnoreCase(frontendRoundType) && s.getAdmissionRoute() != null) {
                    actualRoute = s.getAdmissionRoute();
                }
            }
        }

        String rtNorm = roundPhaseNormalizer.normalizeRoundType(actualRoute);
        if ("NON_CUET".equalsIgnoreCase(rtNorm)) rtNorm = "NONCUET";
        rtNorm = rtNorm.toUpperCase(Locale.ROOT);
        // =====================================================================

        var programme = programmeRepository.findById(programmeId)
                .orElseThrow(() -> new EntityNotFoundException("Programme not found"));

        var stream = programme.getStream();

        // Determine which engines to run based on the true route
        boolean processCuet = "CUET".equals(rtNorm) || "COMBINED".equals(rtNorm);
        boolean processNonCuet = "NONCUET".equals(rtNorm) || "COMBINED".equals(rtNorm);

        // Clean slate for the lists we are about to generate
        if (processCuet) {
            persistenceService.hardResetByProgrammeRoundPhase(admissionWindowId, programmeId, "CUET", phNorm);
        }
        if (processNonCuet) {
            persistenceService.hardResetByProgrammeRoundPhase(admissionWindowId, programmeId, "NONCUET", phNorm);
        }

        MeritListMetadataDTO lastGenerated = null;

        // --- 1. THE CUET ENGINE ---
        if (processCuet) {
            String targetRt = "CUET";
            List<Application> withEntranceApps = applicationRepository.findEligibleByWindowProgrammeAndApplicantType(admissionWindowId, programmeId, ApplicantType.WITH_ENTRANCE);

            if (phNorm > 1 && withEntranceApps != null && !withEntranceApps.isEmpty()) {
                withEntranceApps = withEntranceApps.stream()
                        .filter(app -> !seatAllotmentRepository.existsByAdmissionWindowAdmissionIdAndRoundTypeAndPhaseNoLessThanAndApplicationApplicationIdAndStatus(admissionWindowId, targetRt, phNorm, app.getApplicationId(), AllotmentStatus.ACCEPTED))
                        .collect(Collectors.toList());
            }

            if (withEntranceApps != null && !withEntranceApps.isEmpty()) {
                lastGenerated = isUG ? meritListGenerator.generateUGListForApplicantType(window, stream, programme, withEntranceApps, ApplicantType.WITH_ENTRANCE, targetRt, phNorm)
                        : meritListGenerator.generatePGListForApplicantType(window, programme, withEntranceApps, ApplicantType.WITH_ENTRANCE, targetRt, phNorm);
            }
        }

        // --- 2. THE NON-CUET ENGINE ---
        if (processNonCuet) {
            // Fixed: was "NON_CU_ET" (typo) — corrected to "NONCUET"
            String targetRt = "NONCUET";
            boolean isNonCuetPhase1 = phNorm == 1;

            List<Application> withoutEntranceApps = applicationRepository.findEligibleByWindowProgrammeAndApplicantType(admissionWindowId, programmeId, ApplicantType.WITHOUT_ENTRANCE);

            // If it's Phase 1 for Non-CUET, ensure they haven't already accepted a CUET seat
            if (isNonCuetPhase1 && withoutEntranceApps != null && !withoutEntranceApps.isEmpty()) {
                withoutEntranceApps = withoutEntranceApps.stream()
                        .filter(app -> !seatAllotmentRepository.existsByAdmissionWindowAdmissionIdAndRoundTypeAndApplicationApplicationIdAndStatus(admissionWindowId, "CUET", app.getApplicationId(), AllotmentStatus.ACCEPTED))
                        .toList();
            }

            if (phNorm > 1 && withoutEntranceApps != null && !withoutEntranceApps.isEmpty()) {
                withoutEntranceApps = withoutEntranceApps.stream()
                        .filter(app -> !seatAllotmentRepository.existsByAdmissionWindowAdmissionIdAndRoundTypeAndPhaseNoLessThanAndApplicationApplicationIdAndStatus(admissionWindowId, targetRt, phNorm, app.getApplicationId(), AllotmentStatus.ACCEPTED))
                        .collect(Collectors.toList());
            }

            if (withoutEntranceApps != null && !withoutEntranceApps.isEmpty()) {
                lastGenerated = isUG ? meritListGenerator.generateUGListForApplicantType(window, stream, programme, withoutEntranceApps, ApplicantType.WITHOUT_ENTRANCE, targetRt, phNorm)
                        : meritListGenerator.generatePGListForApplicantType(window, programme, withoutEntranceApps, ApplicantType.WITHOUT_ENTRANCE, targetRt, phNorm);
            }
        }

        return lastGenerated;
    }

    @Override
    @Transactional(readOnly = true)
    public MeritListResponseDTO getMeritListById(Long meritListId) {
        MeritList meritList = meritListRepository.findById(meritListId)
                .orElseThrow(() -> new EntityNotFoundException("Merit list not found"));
        List<MeritListEntry> entries = entryRepository.findByMeritListOrderByRank(meritListId);
        return MeritListResponseDTO.builder()
                .metadata(meritListMapper.toMetadataDTO(meritList))
                .entries(entries.stream().map(meritListMapper::toRowDTO).collect(Collectors.toList()))
                .build();
    }

    @Transactional(readOnly = true)
    public PagedResponse<MeritListRowDTO> getPagedMeritListById(Long meritListId, int page, int size) {
        meritListRepository.findById(meritListId).orElseThrow(() -> new EntityNotFoundException("Merit list not found"));
        Pageable pageable = PageRequest.of(page, size);
        Page<MeritListEntry> entriesPage = entryRepository.findByMeritListIdPaged(meritListId, pageable);
        List<MeritListRowDTO> dtoList = entriesPage.getContent().stream().map(meritListMapper::toRowDTO).collect(Collectors.toList());
        return new PagedResponse<>(dtoList, entriesPage.getNumber(), entriesPage.getSize(), entriesPage.getTotalElements(), entriesPage.getTotalPages(), entriesPage.isLast());
    }

    @Override
    public void publishMeritList(Long meritListId) {
        MeritList meritList = meritListRepository.findById(meritListId).orElseThrow(() -> new EntityNotFoundException("Merit list not found"));
        if (!"DRAFT".equals(meritList.getStatus())) throw new IllegalStateException("Only draft merit lists can be published");
        meritList.setStatus(STATUS_PUBLISHED);
        meritListRepository.save(meritList);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasMeritListForUG(String admissionWindowCode, Short programmeId, String roundType, Integer phaseNo) {
        AdmissionWindow window = admissionWindowRepository.findByAdmissionCode(admissionWindowCode)
                .orElseThrow(() -> new EntityNotFoundException("Admission window not found: " + admissionWindowCode));
        Short admissionWindowId = window.getAdmissionId();

        String rt = roundPhaseNormalizer.normalizeRoundType(roundType);
        Integer ph = roundPhaseNormalizer.normalizePhaseNo(phaseNo);
        return !meritListRepository.findAllByAdmissionWindowAndProgrammeOrderedByTypeForRoundAndPhase(admissionWindowId, programmeId, rt, ph).isEmpty();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasMeritListForPG(String admissionWindowCode, Short programmeId, String roundType, Integer phaseNo) {
        AdmissionWindow window = admissionWindowRepository.findByAdmissionCode(admissionWindowCode)
                .orElseThrow(() -> new EntityNotFoundException("Admission window not found: " + admissionWindowCode));
        Short admissionWindowId = window.getAdmissionId();

        String rt = roundPhaseNormalizer.normalizeRoundType(roundType);
        Integer ph = roundPhaseNormalizer.normalizePhaseNo(phaseNo);
        return !meritListRepository.findAllByAdmissionWindowAndProgrammeOrderedByTypeForRoundAndPhase(admissionWindowId, programmeId, rt, ph).isEmpty();
    }

    @Override
    @Transactional(readOnly = true)
    public ApplicantCountDTO countApplicantsForUG(String admissionWindowCode, Short programmeId, String roundType, Integer phaseNo) {
        AdmissionWindow window = admissionWindowRepository.findByAdmissionCode(admissionWindowCode)
                .orElseThrow(() -> new EntityNotFoundException("Admission window not found: " + admissionWindowCode));
        Short admissionWindowId = window.getAdmissionId();

        int totalComplete = applicationRepository.findByAdmissionWindow_AdmissionIdAndAdmissionWindow_AdmissionWindowProgrammes_Programme_ProgrammeIdAndApplicationStatus(admissionWindowId, programmeId, "SUBMITTED").size();

        int eligible = applicationRepository.findEligibleByWindowProgrammeAndApplicantType(admissionWindowId, programmeId, ApplicantType.WITH_ENTRANCE).size() +
                applicationRepository.findEligibleByWindowProgrammeAndApplicantType(admissionWindowId, programmeId, ApplicantType.WITHOUT_ENTRANCE).size();
        return new ApplicantCountDTO(totalComplete, eligible);
    }

    @Override
    @Transactional(readOnly = true)
    public ApplicantCountDTO countApplicantsForPG(String admissionWindowCode, Short programmeId, String roundType, Integer phaseNo) {
        AdmissionWindow window = admissionWindowRepository.findByAdmissionCode(admissionWindowCode)
                .orElseThrow(() -> new EntityNotFoundException("Admission window not found: " + admissionWindowCode));
        Short admissionWindowId = window.getAdmissionId();

        int totalComplete = applicationRepository.findByAdmissionWindow_AdmissionIdAndAdmissionWindow_AdmissionWindowProgrammes_Programme_ProgrammeIdAndApplicationStatus(admissionWindowId, programmeId, "SUBMITTED").size();
        int eligible = applicationRepository.findEligibleByWindowProgrammeAndApplicantType(admissionWindowId, programmeId, ApplicantType.WITH_ENTRANCE).size() +
                applicationRepository.findEligibleByWindowProgrammeAndApplicantType(admissionWindowId, programmeId, ApplicantType.WITHOUT_ENTRANCE).size();
        return new ApplicantCountDTO(totalComplete, eligible);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MeritListResponseDTO> getAllMeritListsForUGStream(String admissionWindowCode, Short streamId, String roundType, Integer phaseNo) {
        AdmissionWindow window = admissionWindowRepository.findByAdmissionCode(admissionWindowCode)
                .orElseThrow(() -> new EntityNotFoundException("Admission window not found: " + admissionWindowCode));
        Short admissionWindowId = window.getAdmissionId();

        String rt = roundPhaseNormalizer.normalizeRoundType(roundType);
        return meritListRepository.findAllByAdmissionWindow_AdmissionIdAndStream_StreamId(admissionWindowId, streamId)
                .stream()
                .filter(ml -> rt.equalsIgnoreCase(ml.getRoundType()) && (phaseNo == null || phaseNo.equals(ml.getPhaseNo())))
                .map(ml -> getMeritListById(ml.getMeritListId()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MeritListResponseDTO> getAllMeritListsForPGProgramme(String admissionWindowCode, Short programmeId, String roundType, Integer phaseNo) {
        AdmissionWindow window = admissionWindowRepository.findByAdmissionCode(admissionWindowCode)
                .orElseThrow(() -> new EntityNotFoundException("Admission window not found: " + admissionWindowCode));
        Short admissionWindowId = window.getAdmissionId();

        String rt = roundPhaseNormalizer.normalizeRoundType(roundType);
        return meritListRepository.findAllByAdmissionWindowAndProgrammeOrderedByTypeForRoundAndPhase(admissionWindowId, programmeId, rt, phaseNo)
                .stream()
                .map(ml -> getMeritListById(ml.getMeritListId()))
                .collect(Collectors.toList());
    }

    // Legacy Support Methods
    @Override @Transactional(readOnly = true)
    public boolean hasMeritListForUG(String admissionWindowCode, Short programmeId) {
        AdmissionWindow window = admissionWindowRepository.findByAdmissionCode(admissionWindowCode)
                .orElseThrow(() -> new EntityNotFoundException("Admission window not found: " + admissionWindowCode));
        return meritListRepository.existsByAdmissionWindowAndProgramme(window.getAdmissionId(), programmeId, STATUS_PUBLISHED);
    }

    @Override @Transactional(readOnly = true)
    public boolean hasMeritListForPG(String admissionWindowCode, Short programmeId) {
        AdmissionWindow window = admissionWindowRepository.findByAdmissionCode(admissionWindowCode)
                .orElseThrow(() -> new EntityNotFoundException("Admission window not found: " + admissionWindowCode));
        return meritListRepository.existsByAdmissionWindowAndProgramme(window.getAdmissionId(), programmeId, STATUS_PUBLISHED);
    }

    @Override @Transactional(readOnly = true)
    public ApplicantCountDTO countApplicantsForUG(String admissionWindowCode, Short programmeId) {
        return countApplicantsForUG(admissionWindowCode, programmeId, DEFAULT_ROUND_TYPE, DEFAULT_PHASE_NO);
    }

    @Override @Transactional(readOnly = true)
    public ApplicantCountDTO countApplicantsForPG(String admissionWindowCode, Short programmeId) {
        return countApplicantsForPG(admissionWindowCode, programmeId, DEFAULT_ROUND_TYPE, DEFAULT_PHASE_NO);
    }

    @Override @Transactional(readOnly = true)
    public List<MeritListResponseDTO> getAllMeritListsForUGStream(String admissionWindowCode, Short streamId) {
        AdmissionWindow window = admissionWindowRepository.findByAdmissionCode(admissionWindowCode)
                .orElseThrow(() -> new EntityNotFoundException("Admission window not found: " + admissionWindowCode));
        return meritListRepository.findAllByAdmissionWindow_AdmissionIdAndStream_StreamId(window.getAdmissionId(), streamId)
                .stream().map(ml -> getMeritListById(ml.getMeritListId())).collect(Collectors.toList());
    }

    @Override @Transactional(readOnly = true)
    public List<MeritListResponseDTO> getAllMeritListsForPGProgramme(String admissionWindowCode, Short programmeId) {
        AdmissionWindow window = admissionWindowRepository.findByAdmissionCode(admissionWindowCode)
                .orElseThrow(() -> new EntityNotFoundException("Admission window not found: " + admissionWindowCode));
        return meritListRepository.findAllByAdmissionWindow_AdmissionIdAndProgramme_ProgrammeId(window.getAdmissionId(), programmeId)
                .stream().map(ml -> getMeritListById(ml.getMeritListId())).collect(Collectors.toList());
    }
}
