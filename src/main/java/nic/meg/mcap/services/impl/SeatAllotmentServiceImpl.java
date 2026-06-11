package nic.meg.mcap.services.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nic.meg.mcap.dto.response.AllottedCandidateRowDTO;
import nic.meg.mcap.dto.response.ProgrammeAllocationSummaryDTO;
import nic.meg.mcap.dto.response.SeatAllocationSummaryDTO;
import nic.meg.mcap.dto.response.StudentAllotmentResponseDTO;
import nic.meg.mcap.entities.*;
import nic.meg.mcap.enums.AllotmentStatus;
import nic.meg.mcap.enums.InstituteStatus;
import nic.meg.mcap.repositories.*;
import nic.meg.mcap.services.SeatAllotmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SeatAllotmentServiceImpl implements SeatAllotmentService {

    private final AdmissionWindowRepository admissionWindowRepository;
    private final ProgrammesOfferedRepository programmesOfferedRepository;
    private final SeatMatrixRepository seatMatrixRepository;
    private final SeatReservationRepository seatReservationRepository;
    private final SeatAllotmentRepository seatAllotmentRepository;
    private final MeritListRepository meritListRepository;
    private final MeritListEntryRepository meritListEntryRepository;
    private final ProgrammePreferenceRepository programmePreferenceRepository;
    private final ScheduleRepository scheduleRepository;
    private final InstituteAdmissionPreferenceRepository instituteAdmissionPreferenceRepository;

    // --- Inner Classes for Global Deferred Acceptance Engine ---
    private static class QuotaData {
        int initialOpen;
        Map<String, Integer> initialCategories = new HashMap<>();

        int currentOpen;
        Map<String, Integer> currentCategories = new HashMap<>();

        public void reset() {
            this.currentOpen = initialOpen;
            this.currentCategories.clear();
            this.currentCategories.putAll(initialCategories);
        }
    }

    private static class Proposal {
        Long applicationId;
        Application application;
        Applicant applicant;
        Integer meritRank;
        String categoryCode;
        ApplicantProgrammePreference preference;
        String allottedBucket;

        public Proposal(Long applicationId, Application application, Applicant applicant, Integer meritRank, String categoryCode, ApplicantProgrammePreference preference) {
            this.applicationId = applicationId;
            this.application = application;
            this.applicant = applicant;
            this.meritRank = meritRank;
            this.categoryCode = categoryCode;
            this.preference = preference;
        }
    }

    @Transactional
    @Override
    public SeatAllocationSummaryDTO runAllocationForWindow(String admissionCode, String frontendRoundType, Integer phaseNo) {
        AdmissionWindow window = admissionWindowRepository.findByAdmissionCode(admissionCode)
                .orElseThrow(() -> new EntityNotFoundException("Admission window not found: " + admissionCode));

        Short admissionId = window.getAdmissionId();
        int phase = normalizePhaseNo(phaseNo);
        String rt = normalizeRoundType(frontendRoundType);

        // COMBINED phase: resolve actual route from Schedule, then fork into two independent runs
        if ("COMBINED".equals(rt)) {
            log.info("COMBINED phase detected for windowCode={}, phaseNo={}. Forking into CUET + NONCUET runs.", admissionCode, phase);
            runSingleAllocation(window, admissionId, "CUET", phase);
            runSingleAllocation(window, admissionId, "NONCUET", phase);
            // Return a merged summary covering both routes for this phase
            return getMergedAllocationSummary(admissionCode, phase);
        }

        return runSingleAllocation(window, admissionId, rt, phase);
    }

    /**
     * Runs the full Gale-Shapley allocation for one specific roundType (CUET or NONCUET).
     * This is the core engine — never called with "COMBINED".
     */
    private SeatAllocationSummaryDTO runSingleAllocation(AdmissionWindow window, Short admissionId, String rt, int phase) {
        log.info("Starting GLOBAL Allotment for windowId={}, roundType={}, phaseNo={}", admissionId, rt, phase);

        // 1. CLEAR OLD DATA for this specific round and phase
        seatAllotmentRepository.deleteByAdmissionWindowAdmissionIdAndRoundTypeAndPhaseNo(admissionId, rt, phase);

        // 2. LOAD GLOBAL QUOTAS — only include institutes whose CUET preference matches this round type
        List<ProgrammeOffered> allOfferings = findOfferingsForWindow(window);
        Map<Integer, QuotaData> globalQuotaMap = new HashMap<>();

        // Determine which CUET preference to filter by for this round type.
        // CUET round    → only institutes that opted IN  (wantsCuet = true)
        // NONCUET round → only institutes that opted OUT (wantsCuet = false)
        boolean cuetPreferenceFilter = "CUET".equals(rt);

        // Fetch institute IDs that match this preference for this window
        Set<Short> eligibleInstituteIds = instituteAdmissionPreferenceRepository
                .findInstituteIdsByWindowAndCuetPreference(admissionId, cuetPreferenceFilter);

        for (ProgrammeOffered po : allOfferings) {
            Integer poId = po.getProgrammeOfferedId();

            // Skip institutes that haven't opted for this round type
            Short ownerInstituteId = po.getInstituteDepartment().getInstitute().getInstituteId();
            if (!eligibleInstituteIds.contains(ownerInstituteId)) {
                log.debug("Skipping programmeOfferedId={} (instituteId={}) — not eligible for roundType={}", poId, ownerInstituteId, rt);
                continue;
            }

            SeatMatrix sm = seatMatrixRepository.findByProgrammeOfferedProgrammeOfferedId(poId).orElse(null);

            if (sm == null || !"SUBMITTED".equals(sm.getApprovalStatus())) continue;

            List<SeatReservation> res = seatReservationRepository.findByProgrammeOfferedIdAndAdmissionWindowId(poId, admissionId);
            QuotaData qd = new QuotaData();
            int reservedTotal = res.stream().mapToInt(SeatReservation::getReservedSeats).sum();
            qd.initialOpen = Math.max(0, sm.getTotalSeats() - reservedTotal);

            for (SeatReservation sr : res) {
                String bucket = (sr.getCommunityCategory() != null) ? sr.getCommunityCategory().getCategoryCode() : sr.getReservationType().name();
                qd.initialCategories.merge(bucket, sr.getReservedSeats(), Integer::sum);
            }
            qd.reset();
            globalQuotaMap.put(poId, qd);
        }

        // 3. LOAD GLOBAL RANKS from merit lists for this specific roundType
        Map<Long, Map<Short, Integer>> globalRankMap = new HashMap<>();
        Map<Long, Application> applicationMap = new HashMap<>();
        Set<Long> uniqueApplicants = new HashSet<>();

        List<MeritList> meritLists = meritListRepository.findAllByAdmissionWindowAdmissionIdAndRoundTypeAndPhaseNoOrderByProgrammeProgrammeIdAsc(admissionId, rt, phase);
        for (MeritList ml : meritLists) {
            Short progId = ml.getProgramme().getProgrammeId();
            List<MeritListEntry> entries = meritListEntryRepository.findByMeritListOrderByRank(ml.getMeritListId());
            for (MeritListEntry e : entries) {
                Long appId = e.getApplication().getApplicationId();
                globalRankMap.computeIfAbsent(appId, k -> new HashMap<>()).put(progId, e.getRank());
                applicationMap.put(appId, e.getApplication());
                uniqueApplicants.add(appId);
            }
        }

        // 4. PREPARE GALE-SHAPLEY QUEUE
        Queue<Long> unassigned = new LinkedList<>(uniqueApplicants);
        Map<Long, Integer> currentPrefIdx = new HashMap<>();
        uniqueApplicants.forEach(id -> currentPrefIdx.put(id, 0));

        Map<Integer, List<Proposal>> currentMatches = new HashMap<>();

        // 5. GLOBAL MATCHING LOOP
        while (!unassigned.isEmpty()) {
            Long appId = unassigned.poll();
            Application app = applicationMap.get(appId);
            List<ApplicantProgrammePreference> prefs = programmePreferenceRepository.findByApplicationApplicationIdOrderByPreferenceOrderAsc(appId);

            int idx = currentPrefIdx.get(appId);
            if (prefs == null || idx >= prefs.size()) continue;

            ApplicantProgrammePreference pref = prefs.get(idx);
            Integer targetPoId = pref.getProgrammeOffered().getProgrammeOfferedId();
            Short targetProgId = pref.getProgrammeOffered().getProgramme().getProgrammeId();

            Integer rank = globalRankMap.getOrDefault(appId, Collections.emptyMap()).get(targetProgId);

            if (rank == null || !globalQuotaMap.containsKey(targetPoId)) {
                currentPrefIdx.put(appId, idx + 1);
                unassigned.add(appId);
                continue;
            }

            String catCode = (app.getApplicant().getCommunityCategory() != null)
                    ? app.getApplicant().getCommunityCategory().getCategoryCode() : "OPEN";

            Proposal p = new Proposal(appId, app, app.getApplicant(), rank, catCode, pref);
            currentMatches.computeIfAbsent(targetPoId, k -> new ArrayList<>()).add(p);

            evaluateGlobalProposals(targetPoId, currentMatches.get(targetPoId), globalQuotaMap.get(targetPoId), unassigned, currentPrefIdx);
        }

        // 6. PERSIST ALLOTMENTS — roundType stored as CUET or NONCUET (never COMBINED)
        for (List<Proposal> finalAllotments : currentMatches.values()) {
            for (Proposal p : finalAllotments) {
                SeatAllotment sa = new SeatAllotment();
                sa.setApplication(p.application);
                sa.setApplicant(p.applicant);
                sa.setAdmissionWindow(window);
                sa.setProgrammeOffered(p.preference.getProgrammeOffered());
                sa.setStatus(AllotmentStatus.PENDING_VERIFICATION);
                sa.setRoundType(rt);
                sa.setPhaseNo(phase);
                sa.setReservationUsed(p.allottedBucket);
                seatAllotmentRepository.save(sa);
            }
        }

        return getAllocationSummary(window.getAdmissionCode(), rt, phase);
    }

    /**
     * For a COMBINED phase, merges the CUET and NONCUET summaries into one response
     * so the caller gets a single DTO reflecting both runs.
     */
    private SeatAllocationSummaryDTO getMergedAllocationSummary(String admissionCode, int phase) {
        SeatAllocationSummaryDTO cuetSummary   = getAllocationSummary(admissionCode, "CUET",    phase);
        SeatAllocationSummaryDTO nonCuetSummary = getAllocationSummary(admissionCode, "NONCUET", phase);

        SeatAllocationSummaryDTO merged = new SeatAllocationSummaryDTO();
        merged.setAdmissionCode(admissionCode);
        merged.setTotalProgrammes(cuetSummary.getTotalProgrammes()); // same set of programmes
        merged.setTotalSeats(cuetSummary.getTotalSeats() + nonCuetSummary.getTotalSeats());
        merged.setTotalAllotted(cuetSummary.getTotalAllotted() + nonCuetSummary.getTotalAllotted());
        merged.setTotalUnfilled(cuetSummary.getTotalUnfilled() + nonCuetSummary.getTotalUnfilled());

        // Combine programme-level summaries from both routes
        List<ProgrammeAllocationSummaryDTO> combined = new ArrayList<>();
        combined.addAll(cuetSummary.getProgrammeSummaries());
        combined.addAll(nonCuetSummary.getProgrammeSummaries());
        merged.setProgrammeSummaries(combined);

        // Next-phase availability is based on both routes being settled
        merged.setCanGenerateNextPhase(
                Boolean.TRUE.equals(cuetSummary.isCanGenerateNextPhase()) &&
                        Boolean.TRUE.equals(nonCuetSummary.isCanGenerateNextPhase())
        );

        return merged;
    }

    private void evaluateGlobalProposals(Integer poId, List<Proposal> activeProposals, QuotaData quota, Queue<Long> unassigned, Map<Long, Integer> prefIdxMap) {
        activeProposals.sort(Comparator.comparingInt(p -> p.meritRank));
        quota.reset();

        List<Proposal> accepted = new ArrayList<>();
        List<Proposal> rejected = new ArrayList<>();

        for (Proposal p : activeProposals) {
            if (quota.currentOpen > 0) {
                p.allottedBucket = "OPEN";
                quota.currentOpen--;
                accepted.add(p);
            } else if (quota.currentCategories.getOrDefault(p.categoryCode, 0) > 0) {
                p.allottedBucket = p.categoryCode;
                quota.currentCategories.put(p.categoryCode, quota.currentCategories.get(p.categoryCode) - 1);
                accepted.add(p);
            } else {
                rejected.add(p);
            }
        }

        for (Proposal r : rejected) {
            prefIdxMap.put(r.applicationId, prefIdxMap.get(r.applicationId) + 1);
            unassigned.add(r.applicationId);
        }

        activeProposals.clear();
        activeProposals.addAll(accepted);
    }

    // --- STANDARD FETCH METHODS ---

    @Override
    public List<AllottedCandidateRowDTO> getAllottedCandidates(String admissionCode, String roundType, Integer phaseNo, Integer programmeOfferedId) {
        AdmissionWindow window = admissionWindowRepository.findByAdmissionCode(admissionCode)
                .orElseThrow(() -> new EntityNotFoundException("Admission window not found: " + admissionCode));
        Short admissionId = window.getAdmissionId();

        String rt = normalizeRoundType(roundType);
        int phase = normalizePhaseNo(phaseNo);

        List<SeatAllotment> allotments = seatAllotmentRepository.findByAdmissionWindowAdmissionIdAndRoundTypeAndPhaseNoAndProgrammeOfferedProgrammeOfferedId(
                admissionId, rt, phase, programmeOfferedId);

        List<AllottedCandidateRowDTO> rows = new ArrayList<>();
        for (SeatAllotment sa : allotments) {
            Application app = sa.getApplication();
            Applicant applicant = (app != null) ? app.getApplicant() : null;
            ProgrammeOffered po = sa.getProgrammeOffered();

            AllottedCandidateRowDTO dto = new AllottedCandidateRowDTO();

            if (app != null) {
                dto.setApplicationId(app.getApplicationId());
                dto.setRegistrationNumber(app.getApplicationNo());
            }

            if (applicant != null) {
                String fullName = applicant.getFirstName()
                        + (applicant.getMiddleName() != null ? " " + applicant.getMiddleName() : "")
                        + " " + applicant.getLastName();
                dto.setApplicantName(fullName);
                dto.setCommunityCategory(applicant.getCommunityCategory() != null ? applicant.getCommunityCategory().getCategoryName() : "GENERAL");
            }

            dto.setReservationUsed(sa.getReservationUsed() != null ? sa.getReservationUsed() : "OPEN");

            if (po != null) {
                dto.setProgrammeName(po.getProgramme().getProgrammeName());
                dto.setInstituteName(po.getInstituteDepartment().getInstitute().getInstituteName());
                dto.setShiftName(po.getShift() != null ? po.getShift().name() : "Day");
            }

            dto.setAllotmentStatus(sa.getStatus() != null ? sa.getStatus().name() : AllotmentStatus.PENDING.name());

            if (app != null && po != null) {
                Short programmeId = (po.getProgramme() != null) ? po.getProgramme().getProgrammeId() : null;
                Short streamId = (po.getProgramme() != null && po.getProgramme().getStream() != null) ? po.getProgramme().getStream().getStreamId() : null;

                if (programmeId != null || streamId != null) {
                    List<MeritListEntry> entries = meritListEntryRepository.findEntryForAllotment(admissionId, rt, phase, app.getApplicationId(), programmeId, streamId);
                    if (entries != null && !entries.isEmpty()) {
                        dto.setRank(entries.get(0).getRank());
                        dto.setMeritScore(entries.get(0).getMeritScore());
                    }
                }
            }
            rows.add(dto);
        }
        return rows;
    }

    @Transactional(readOnly = true)
    @Override
    public SeatAllocationSummaryDTO getAllocationSummary(String admissionCode, String roundType, Integer phaseNo) {
        AdmissionWindow window = admissionWindowRepository.findByAdmissionCode(admissionCode)
                .orElseThrow(() -> new EntityNotFoundException("Admission window not found " + admissionCode));
        Short admissionId = window.getAdmissionId();

        String rt = normalizeRoundType(roundType);
        int phase = normalizePhaseNo(phaseNo);

        // For COMBINED, return the merged view of both routes
        if ("COMBINED".equals(rt)) {
            return getMergedAllocationSummary(admissionCode, phase);
        }

        List<ProgrammeOffered> programmes = findOfferingsForWindow(window);
        List<ProgrammeAllocationSummaryDTO> programmeSummaries = new ArrayList<>();
        int totalSeats = 0;
        int totalAllotted = 0;

        for (ProgrammeOffered po : programmes) {
            Integer poId = po.getProgrammeOfferedId();
            Optional<SeatMatrix> seatMatrixOpt = seatMatrixRepository.findByProgrammeOfferedProgrammeOfferedId(poId);
            if (seatMatrixOpt.isEmpty()) continue;

            SeatMatrix seatMatrix = seatMatrixOpt.get();
            int programmeTotalSeats = seatMatrix.getTotalSeats();
            List<SeatReservation> reservations = seatReservationRepository.findByProgrammeOfferedIdAndAdmissionWindowId(poId, admissionId);

            int reservedSeats = reservations.stream().mapToInt(SeatReservation::getReservedSeats).sum();
            int openSeats = Math.max(0, programmeTotalSeats - reservedSeats);
            int allottedForProgramme = seatAllotmentRepository.countByAdmissionWindowAdmissionIdAndRoundTypeAndPhaseNoAndProgrammeOfferedProgrammeOfferedId(admissionId, rt, phase, poId);

            ProgrammeAllocationSummaryDTO dto = new ProgrammeAllocationSummaryDTO();
            dto.setProgrammeOfferedId(poId);
            dto.setProgrammeName(po.getProgramme().getProgrammeName());
            dto.setInstituteName(po.getInstituteDepartment().getInstitute().getInstituteName());
            dto.setTotalSeats(programmeTotalSeats);
            dto.setReservedSeats(reservedSeats);
            dto.setOpenSeats(openSeats);
            dto.setAllottedCount(allottedForProgramme);
            dto.setUnfilledSeats(Math.max(0, programmeTotalSeats - allottedForProgramme));

            programmeSummaries.add(dto);
            totalSeats += programmeTotalSeats;
            totalAllotted += allottedForProgramme;
        }

        SeatAllocationSummaryDTO result = new SeatAllocationSummaryDTO();
        result.setAdmissionCode(admissionCode);
        result.setTotalProgrammes(programmes.size());
        result.setTotalSeats(totalSeats);
        result.setTotalAllotted(totalAllotted);
        result.setTotalUnfilled(totalSeats - totalAllotted);
        result.setProgrammeSummaries(programmeSummaries);

        long totalPending = seatAllotmentRepository.countByAdmissionWindowAdmissionIdAndRoundTypeAndPhaseNoAndStatus(admissionId, rt, phase, AllotmentStatus.PENDING)
                + seatAllotmentRepository.countByAdmissionWindowAdmissionIdAndRoundTypeAndPhaseNoAndStatus(admissionId, rt, phase, AllotmentStatus.PENDING_VERIFICATION);

        if (totalPending == 0) {
            List<Integer> configuredPhases = scheduleRepository.findDistinctPhasesForWindowAndRound(admissionId, rt);
            Optional<Integer> nextPh = configuredPhases.stream().filter(p -> p > phase).min(Integer::compareTo);
            if (nextPh.isPresent()) {
                result.setCanGenerateNextPhase(true);
                result.setNextPhaseNumber(nextPh.get());
            } else if (scheduleRepository.findDistinctRoundsForWindow(admissionId).contains("NON_CUET")) {
                result.setCanStartNonCuet(true);
            }
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentAllotmentResponseDTO> getStudentAllotmentsByInstitute(Integer instituteId) {
        return seatAllotmentRepository.findAllByInstituteId(instituteId).stream()
                .map(this::convertToStudentAllotmentResponseDTO).collect(Collectors.toList());
    }

    private StudentAllotmentResponseDTO convertToStudentAllotmentResponseDTO(SeatAllotment sa) {
        StudentAllotmentResponseDTO dto = new StudentAllotmentResponseDTO();
        dto.setAllotmentId(sa.getId());
        dto.setStudentName(sa.getApplicant().getFirstName() + " " + sa.getApplicant().getLastName());
        dto.setStudentEmail(sa.getApplicant().getEmail());
        dto.setStudentPhone(sa.getApplicant().getPhoneNumber());
        dto.setApplicationNumber(sa.getApplication().getApplicationNo());
        dto.setProgrammeName(sa.getProgrammeOffered().getProgramme().getProgrammeName());
        dto.setDepartmentName(sa.getProgrammeOffered().getInstituteDepartment().getDepartment().getDepartmentName());
        dto.setShiftName(sa.getProgrammeOffered().getShift() != null ? sa.getProgrammeOffered().getShift().name() : "Day");
        dto.setAllotmentStatus(sa.getStatus());
        return dto;
    }

    @Override
    public Long countAllotmentsByInstitute(Short instituteId) {
        return seatAllotmentRepository.countByInstituteIdAndStatus(instituteId, AllotmentStatus.ACCEPTED);
    }

    @Override
    public Long countAcceptedAllotmentsByInstitute(Short instituteId) {
        return countAllotmentsByInstitute(instituteId);
    }

    @Override
    public List<ProgrammeAllocationSummaryDTO> getInstituteProgrammeSummary(Short instituteId, String shiftStr) {
        return programmesOfferedRepository.findByInstituteDepartment_Institute_InstituteIdAndShift(instituteId, nic.meg.mcap.enums.Shift.valueOf(shiftStr.toUpperCase()))
                .stream().map(po -> {
                    ProgrammeAllocationSummaryDTO d = new ProgrammeAllocationSummaryDTO();
                    d.setProgrammeOfferedId(po.getProgrammeOfferedId());
                    d.setProgrammeName(po.getProgramme().getProgrammeName());
                    d.setAllottedCount(seatAllotmentRepository.countByProgrammeOfferedProgrammeOfferedId(po.getProgrammeOfferedId()));
                    seatMatrixRepository.findByProgrammeOfferedProgrammeOfferedId(po.getProgrammeOfferedId()).ifPresent(m -> d.setTotalSeats(m.getTotalSeats()));
                    return d;
                }).collect(Collectors.toList());
    }

    @Override
    public List<ProgrammeAllocationSummaryDTO> getProgrammeAllocationSummary(String admissionCode, Short programmeId, String roundType, Integer phaseNo) {
        AdmissionWindow window = admissionWindowRepository.findByAdmissionCode(admissionCode).orElseThrow();
        return programmesOfferedRepository.findByProgrammeProgrammeId(programmeId, InstituteStatus.ACCEPTED).stream().map(po -> {
            ProgrammeAllocationSummaryDTO d = new ProgrammeAllocationSummaryDTO();
            d.setProgrammeOfferedId(po.getProgrammeOfferedId());
            d.setProgrammeName(po.getProgramme().getProgrammeName());
            d.setInstituteName(po.getInstituteDepartment().getInstitute().getInstituteName());
            d.setShiftName(po.getShift() != null ? po.getShift().name() : "Day");
            seatMatrixRepository.findByProgrammeOfferedProgrammeOfferedId(po.getProgrammeOfferedId()).ifPresent(m -> {
                d.setTotalSeats(m.getTotalSeats() != null ? m.getTotalSeats() : 0);
                int res = seatReservationRepository.findByProgrammeOfferedIdAndAdmissionWindowId(po.getProgrammeOfferedId(), window.getAdmissionId())
                        .stream().mapToInt(SeatReservation::getReservedSeats).sum();
                d.setReservedSeats(res);
                d.setOpenSeats(Math.max(0, (m.getTotalSeats() != null ? m.getTotalSeats() : 0) - res));
            });
            if (d.getTotalSeats() == null) {
                d.setTotalSeats(0);
                d.setReservedSeats(0);
                d.setOpenSeats(0);
            }
            int allot = seatAllotmentRepository.countByAdmissionWindowAdmissionIdAndRoundTypeAndPhaseNoAndProgrammeOfferedProgrammeOfferedId(window.getAdmissionId(), roundType, phaseNo, po.getProgrammeOfferedId());
            d.setAllottedCount(allot);
            d.setUnfilledSeats(Math.max(0, d.getTotalSeats() - allot));
            return d;
        }).collect(Collectors.toList());
    }

    private String normalizeRoundType(String roundType) {
        String rt = (roundType == null) ? "CUET" : roundType.trim().toUpperCase(Locale.ROOT);
        if ("NON_CUET".equals(rt)) rt = "NONCUET";
        return rt;
    }

    private int normalizePhaseNo(Integer phaseNo) {
        return (phaseNo == null || phaseNo < 1) ? 1 : phaseNo;
    }

    @Override
    public int countAllotments(String admissionCode, String roundType, Integer phaseNo, Integer programmeOfferedId) {
        AdmissionWindow w = admissionWindowRepository.findByAdmissionCode(admissionCode).orElseThrow();
        return seatAllotmentRepository.countByAdmissionWindowAdmissionIdAndRoundTypeAndPhaseNoAndProgrammeOfferedProgrammeOfferedId(w.getAdmissionId(), normalizeRoundType(roundType), normalizePhaseNo(phaseNo), programmeOfferedId);
    }

    /**
     * Returns all ProgrammeOffered records applicable to an admission window.
     *
     * - Specific-stream window (Arts / Science / Commerce): fetches only that stream.
     * - "All Streams" window (stream == null, e.g. FYUG): fetches by programme level
     *   across every stream that has offerings — no hardcoded stream IDs.
     */
    private List<ProgrammeOffered> findOfferingsForWindow(AdmissionWindow window) {
        if (window.getStream() != null && window.getStream().getStreamId() != null) {
            return programmesOfferedRepository.findByStreamProgramme(
                    List.of(window.getStream().getStreamId()),
                    window.getProgrammeLevel()
            );
        }
        return (List<ProgrammeOffered>) programmesOfferedRepository
                .findByProgramme_ProgrammeLevel(window.getProgrammeLevel());
    }
}