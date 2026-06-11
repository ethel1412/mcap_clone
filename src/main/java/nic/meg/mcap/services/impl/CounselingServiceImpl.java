package nic.meg.mcap.services.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import nic.meg.mcap.dto.request.SubjectPreferenceRequestDTO;
import nic.meg.mcap.dto.request.VerificationRequestDTO;
import nic.meg.mcap.dto.response.CounselingRoundResponseDTO;
import nic.meg.mcap.dto.response.InstituteAllotmentDTO;
import nic.meg.mcap.dto.response.PagedResponse;
import nic.meg.mcap.dto.response.SeatAllotmentResponseDTO;
import nic.meg.mcap.dto.response.SubjectPreferenceResponseDTO;
import nic.meg.mcap.entities.AdmissionWindow;
import nic.meg.mcap.entities.Applicant;
import nic.meg.mcap.entities.ApplicantSubjectPreference;
import nic.meg.mcap.entities.Application;
import nic.meg.mcap.entities.SeatAllotment;
import nic.meg.mcap.enums.AllotmentStatus;
import nic.meg.mcap.enums.SubjectType;
import nic.meg.mcap.repositories.AdmissionWindowRepository;
import nic.meg.mcap.repositories.ApplicantRepository;
import nic.meg.mcap.repositories.ApplicantSubjectPreferenceRepository;
import nic.meg.mcap.repositories.SeatAllotmentRepository;
import nic.meg.mcap.repositories.ScheduleRepository;
import nic.meg.mcap.repositories.SubjectRepository;
import nic.meg.mcap.services.CounselingService;

@Service
@Transactional
public class CounselingServiceImpl implements CounselingService {

    private static final Logger logger = LoggerFactory.getLogger(CounselingServiceImpl.class);

    @Autowired
    private ApplicantRepository applicantRepository;
    @Autowired
    private SeatAllotmentRepository seatAllotmentRepository;
    @Autowired
    private AdmissionWindowRepository admissionWindowRepository;
    @Autowired
    private ApplicantSubjectPreferenceRepository preferenceRepository;
    @Autowired
    private SubjectRepository subjectRepository;
    @Autowired
    private ScheduleRepository scheduleRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CounselingRoundResponseDTO> getApplicantAllotmentOverviews(String applicantNo) {
        Applicant applicant = applicantRepository.findByApplicantNo(applicantNo)
                .orElseThrow(() -> new EntityNotFoundException("Applicant not found"));

        Set<AdmissionWindow> relevantWindows = applicant.getApplications().stream().map(Application::getAdmissionWindow)
                .filter(Objects::nonNull).collect(Collectors.toSet());

        relevantWindows.forEach(System.out::println);

        List<SeatAllotment> allAllotments = seatAllotmentRepository.findByApplicant(applicant);

        Map<Short, List<SeatAllotment>> byWindow = allAllotments.stream().filter(sa -> sa.getAdmissionWindow() != null)
                .collect(Collectors.groupingBy(sa -> sa.getAdmissionWindow().getAdmissionId()));

        List<CounselingRoundResponseDTO> result = new ArrayList<>();

        for (AdmissionWindow window : relevantWindows) {
            String title = (window.getStream() != null ? window.getStream().getStreamName() : "All Streams") + " (" + window.getSession() + ")";
            Short windowId = window.getAdmissionId();

            List<SeatAllotment> windowAllotments = byWindow.getOrDefault(windowId, Collections.emptyList());

            if (windowAllotments.isEmpty()) {
                result.add(new CounselingRoundResponseDTO((long) windowId, title, "CUET", 1, "NOT_ALLOTTED", null));
                continue;
            }

            Map<String, List<SeatAllotment>> byRoundPhase = windowAllotments.stream()
                    .collect(Collectors.groupingBy(sa -> (sa.getRoundType() == null ? "CUET" : sa.getRoundType()) + "#"
                            + (sa.getPhaseNo() == null ? 1 : sa.getPhaseNo())));

            for (List<SeatAllotment> group : byRoundPhase.values()) {
                SeatAllotment latest = group.stream().max(Comparator.comparing(SeatAllotment::getId)).orElse(null);
                String roundType = (latest != null && latest.getRoundType() != null) ? latest.getRoundType() : "CUET";
                Integer phaseNo = (latest != null && latest.getPhaseNo() != null) ? latest.getPhaseNo() : 1;
                String status = (latest != null && latest.getStatus() != null) ? latest.getStatus().name()
                        : "NOT_ALLOTTED";
                Long allotmentId = (latest != null) ? latest.getId() : null;

                result.add(new CounselingRoundResponseDTO((long) windowId, title, roundType, phaseNo, status,
                        allotmentId));
            }
        }
        result.sort(Comparator
                .comparing(CounselingRoundResponseDTO::getStepName, Comparator.nullsLast(String::compareTo))
                .thenComparing(CounselingRoundResponseDTO::getRoundType, Comparator.nullsLast(String::compareTo))
                .thenComparing(CounselingRoundResponseDTO::getPhaseNo, Comparator.nullsLast(Integer::compareTo)));
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public SeatAllotmentResponseDTO getSeatAllotmentForWindow(String applicantNo, Short admissionWindowId) {
        Applicant applicant = applicantRepository.findByApplicantNo(applicantNo)
                .orElseThrow(() -> new EntityNotFoundException("Applicant not found"));

        List<SeatAllotment> windowAllotments = seatAllotmentRepository
                .findByApplicantAndAdmissionWindowAdmissionIdOrderByIdDesc(applicant, admissionWindowId);

        if (windowAllotments.isEmpty()) {
            AdmissionWindow window = admissionWindowRepository.findById(admissionWindowId)
                    .orElseThrow(() -> new EntityNotFoundException("Admission Window not found"));
            return SeatAllotmentResponseDTO.builder().status("NOT_ALLOTTED")
                    .roundName((window.getStream() != null ? window.getStream().getStreamName() : "All Streams") + " (" + window.getSession() + ")")
                    .admissionWindowId(admissionWindowId).roundType("CUET").phaseNo(1).build();
        }

        return convertToSeatAllotmentResponseDTO(windowAllotments.get(0));
    }

    @Override
    @Transactional(readOnly = true)
    public SeatAllotmentResponseDTO getSeatAllotmentDetailsById(String applicantNo, Long allotmentId) {
        SeatAllotment allotment = seatAllotmentRepository.findByIdWithDetails(allotmentId)
                .orElseThrow(() -> new EntityNotFoundException("Allotment record not found"));

        if (!allotment.getApplicant().getApplicantNo().equals(applicantNo)) {
            throw new SecurityException("Unauthorized access to allotment record");
        }

        return convertToSeatAllotmentResponseDTO(allotment);
    }

    @Override
    public void acceptAllotment(String applicantNo, Long allotmentId) {
        SeatAllotment allotment = seatAllotmentRepository.findById(allotmentId)
                .orElseThrow(() -> new EntityNotFoundException("Allotment record not found"));

        if (!allotment.getApplicant().getApplicantNo().equals(applicantNo))
            throw new SecurityException("Unauthorized");

        if (allotment.getStatus() != AllotmentStatus.PENDING) {
            throw new IllegalStateException("Cannot action allotment in status: " + allotment.getStatus());
        }

        allotment.setStatus(AllotmentStatus.ACCEPTED);
        seatAllotmentRepository.save(allotment);
    }

    @Override
    public void rejectAllotment(String applicantNo, Long allotmentId) {
        SeatAllotment allotment = seatAllotmentRepository.findById(allotmentId)
                .orElseThrow(() -> new EntityNotFoundException("Allotment record not found"));

        if (!allotment.getApplicant().getApplicantNo().equals(applicantNo))
            throw new SecurityException("Unauthorized");

        if (allotment.getStatus() != AllotmentStatus.PENDING) {
            throw new IllegalStateException("Cannot action allotment in status: " + allotment.getStatus());
        }

        allotment.setStatus(AllotmentStatus.REJECTED);
        seatAllotmentRepository.save(allotment);
    }

    @Override
    public void slideUpAllotment(String applicantNo, Long allotmentId) {
        SeatAllotment allotment = seatAllotmentRepository.findById(allotmentId)
                .orElseThrow(() -> new EntityNotFoundException("Allotment record not found"));

        if (!allotment.getApplicant().getApplicantNo().equals(applicantNo))
            throw new SecurityException("Unauthorized");

        if (allotment.getStatus() != AllotmentStatus.PENDING) {
            throw new IllegalStateException("Slide Up is only allowed on a PENDING allotment. Current status: " + allotment.getStatus());
        }

        allotment.setStatus(AllotmentStatus.SLIDE_UP);
        // Retain the existing deadline so the scheduler still knows when to auto-expire
        seatAllotmentRepository.save(allotment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InstituteAllotmentDTO> getPendingVerificationAllotmentsForInstitute(Short instituteId) {
        List<SeatAllotment> allotments = seatAllotmentRepository.findByInstituteAndStatusWithDetails(instituteId,
                AllotmentStatus.PENDING_VERIFICATION);

        return allotments.stream().map(sa -> convertToInstituteDto(sa)).filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InstituteAllotmentDTO> getApplicantsForProgrammeAndStatus(Integer programmeOfferedId,
                                                                          AllotmentStatus status) {
        List<SeatAllotment> allotments = seatAllotmentRepository
                .findByProgrammeOfferedProgrammeOfferedIdAndStatus(programmeOfferedId, status);

        return allotments.stream().map(this::convertToInstituteDto).collect(Collectors.toList());
    }

    @Override
    public void performVerification(Long allotmentId, VerificationRequestDTO request, Short instituteId) {
        SeatAllotment allotment = seatAllotmentRepository.findById(allotmentId)
                .orElseThrow(() -> new EntityNotFoundException("Allotment not found"));

        if (!allotment.getProgrammeOffered().getInstituteDepartment().getInstitute().getInstituteId()
                .equals(instituteId)) {
            throw new SecurityException("Forbidden");
        }

        if (request.getStatus() == AllotmentStatus.INSTITUTE_REJECTED) {
            if (request.getRemarks() == null || request.getRemarks().isBlank()) {
                throw new IllegalArgumentException("Remarks mandatory for rejection");
            }
            allotment.setStatus(AllotmentStatus.INSTITUTE_REJECTED);
        } else {
            allotment.setStatus(AllotmentStatus.PENDING);

            // Derive the decision deadline from the Seat Acceptance schedule step.
            // The step name is e.g. "CUET Phase 1: Seat Acceptance and Admission Fee Payment"
            // and its endDate is when applicants must respond by.
            // Falls back to +72h if the counselling schedule hasn't been created yet.
            Short windowId   = allotment.getAdmissionWindow().getAdmissionId();
            String roundType = allotment.getRoundType();
            Integer phaseNo  = allotment.getPhaseNo();

            LocalDateTime deadline = scheduleRepository
                    .findSeatAcceptanceStep(windowId, roundType, phaseNo)
                    .map(nic.meg.mcap.entities.Schedule::getEndDate)
                    .orElseGet(() -> {
                        return LocalDateTime.now().plusHours(72);
                    });

            allotment.setDecisionDeadline(deadline);
        }

        allotment.setVerificationRemarks(request.getRemarks());
        seatAllotmentRepository.save(allotment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeatAllotmentResponseDTO> getAllotmentsForApplicant(String applicantNo) {
        Applicant applicant = applicantRepository.findByApplicantNo(applicantNo)
                .orElseThrow(() -> new EntityNotFoundException("Applicant not found"));
        return seatAllotmentRepository.findByApplicant(applicant).stream().map(this::convertToSeatAllotmentResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PagedResponse<InstituteAllotmentDTO> getPagedAllotmentsByStatus(Short instituteId,
                                                                           List<AllotmentStatus> statuses, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SeatAllotment> resultPage = seatAllotmentRepository.findByInstituteIdAndStatusInPaged(instituteId,
                statuses, pageable);

        List<InstituteAllotmentDTO> data = resultPage.getContent().stream().map(this::convertToInstituteDto)
                .collect(Collectors.toList());

        return new PagedResponse<>(data, resultPage.getNumber(), resultPage.getSize(), resultPage.getTotalElements(),
                resultPage.getTotalPages(), resultPage.isLast());
    }

    @Override
    @Transactional(readOnly = true)
    public SeatAllotmentResponseDTO getLatestSeatAllotment(String applicantNo) {
        List<SeatAllotment> allotments = seatAllotmentRepository.findByApplicant_ApplicantNoOrderByIdDesc(applicantNo);
        if (allotments.isEmpty()) {
            return SeatAllotmentResponseDTO.builder().status("NOT_ALLOTTED").build();
        }
        return convertToSeatAllotmentResponseDTO(allotments.get(0));
    }

    @Override
    public PagedResponse<InstituteAllotmentDTO> getPagedAllotmentsByStatus(Short instituteId, List<AllotmentStatus> statuses, Short programmeId, int page, int size) {
        return null;
    }

    private SeatAllotmentResponseDTO convertToSeatAllotmentResponseDTO(SeatAllotment sa) {
        if (sa == null)
            return null;

        AdmissionWindow window = sa.getAdmissionWindow();
        String roundName = (window != null && window.getStream() != null)
                ? window.getStream().getStreamName() + " (" + window.getSession() + ")"
                : "General Counseling";

        return SeatAllotmentResponseDTO.builder().allotmentId(sa.getId()).status(sa.getStatus().name())
                .roundName(roundName).roundType(sa.getRoundType()).phaseNo(sa.getPhaseNo())
                .admissionWindowId(window != null ? window.getAdmissionId() : null)
                .allottedProgramme(
                        sa.getProgrammeOffered() != null ? sa.getProgrammeOffered().getProgramme().getProgrammeName()
                                : "N/A")
                .allottedInstitute(sa.getProgrammeOffered() != null
                        ? sa.getProgrammeOffered().getInstituteDepartment().getInstitute().getInstituteName()
                        : "N/A")
                .shiftName(sa.getChosenShift() != null ? sa.getChosenShift().getDisplayName() : "Day")
                .preferenceNumber(0)
                .verificationRemarks(sa.getVerificationRemarks())
                .decisionDeadline(sa.getDecisionDeadline())
                .build();
    }

    private InstituteAllotmentDTO convertToInstituteDto(SeatAllotment sa) {
        if (sa == null)
            return null;
        Applicant applicant = sa.getApplicant();
        String fullName = applicant.getFirstName()
                + (applicant.getMiddleName() != null ? " " + applicant.getMiddleName() : "") + " "
                + applicant.getLastName();

        return InstituteAllotmentDTO.builder().allotmentId(sa.getId()).applicantName(fullName.trim())
                .applicationNo(sa.getApplication().getApplicationNo())
                .programmeName(sa.getProgrammeOffered().getProgramme().getProgrammeName())
                .allottedCategory(sa.getReservationUsed()).roundAndPhase(sa.getRoundType() + " / Ph " + sa.getPhaseNo())
                .remarks(sa.getVerificationRemarks()).build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InstituteAllotmentDTO> getAllotmentsByStatusList(Short instituteId, List<AllotmentStatus> statuses) {
        return seatAllotmentRepository
                .findByProgrammeOffered_InstituteDepartment_Institute_InstituteIdAndStatusIn(instituteId, statuses)
                .stream().map(this::convertToInstituteDto).collect(Collectors.toList());
    }

    @Override
    public void saveCombinationPreferences(String applicantNo, SubjectPreferenceRequestDTO requestDTO) {
        SeatAllotment allotment = seatAllotmentRepository.findById(requestDTO.getSeatAllotmentId())
                .orElseThrow(() -> new EntityNotFoundException("Allotment not found"));

        if (!allotment.getApplicant().getApplicantNo().equals(applicantNo))
            throw new AccessDeniedException("Forbidden");
        if (allotment.getStatus() != AllotmentStatus.ACCEPTED)
            throw new IllegalStateException("Must accept seat first");

        allotment.setChosenShift(requestDTO.getChosenShift());
        preferenceRepository.deleteBySeatAllotment(allotment);

        List<ApplicantSubjectPreference> newPreferences = new ArrayList<>();
        for (Map.Entry<String, List<Integer>> entry : requestDTO.getPreferences().entrySet()) {
            SubjectType type = SubjectType.from(entry.getKey());
            List<Integer> ids = entry.getValue();
            if (type != null && ids != null) {
                for (int i = 0; i < ids.size(); i++) {
                    ApplicantSubjectPreference pref = new ApplicantSubjectPreference();
                    pref.setSeatAllotment(allotment);
                    pref.setSubject(subjectRepository.getReferenceById(ids.get(i)));
                    pref.setSubjectType(type);
                    pref.setPreferenceOrder(i + 1);
                    newPreferences.add(pref);
                }
            }
        }
        preferenceRepository.saveAll(newPreferences);
    }

    @Override
    @Transactional(readOnly = true)
    public SubjectPreferenceResponseDTO getSavedPreferences(String applicantNo, Long allotmentId) {
        SeatAllotment allotment = seatAllotmentRepository.findById(allotmentId)
                .orElseThrow(() -> new EntityNotFoundException("Not found"));
        if (!allotment.getApplicant().getApplicantNo().equals(applicantNo))
            throw new AccessDeniedException("Forbidden");

        SubjectPreferenceResponseDTO response = new SubjectPreferenceResponseDTO();
        response.setChosenShift(allotment.getChosenShift());
        response.setPreferences(allotment.getSubjectPreferences().stream()
                .sorted(Comparator.comparingInt(ApplicantSubjectPreference::getPreferenceOrder))
                .collect(Collectors.groupingBy(ApplicantSubjectPreference::getSubjectType,
                        Collectors.mapping(p -> p.getSubject().getSubjectId(), Collectors.toList()))));
        return response;
    }
}