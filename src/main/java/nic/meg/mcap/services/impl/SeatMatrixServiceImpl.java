package nic.meg.mcap.services.impl;

import nic.meg.mcap.dto.request.SeatMatrixRequestDTO;
import nic.meg.mcap.dto.response.SeatApprovalRowDTO;
import nic.meg.mcap.dto.response.SeatMatrixResponseDTO;
import nic.meg.mcap.entities.SeatMatrix;
import nic.meg.mcap.entities.AdmissionWindow;
import nic.meg.mcap.entities.ProgrammeOffered;
import nic.meg.mcap.repositories.InstituteAdmissionPreferenceRepository;
import nic.meg.mcap.repositories.SeatMatrixRepository;
import nic.meg.mcap.repositories.AdmissionWindowRepository;
import nic.meg.mcap.repositories.ProgrammesOfferedRepository;
import nic.meg.mcap.services.SeatMatrixService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class SeatMatrixServiceImpl implements SeatMatrixService {

    private final SeatMatrixRepository seatMatrixRepository;
    private final ProgrammesOfferedRepository programmesOfferedRepository;
    private final AdmissionWindowRepository admissionWindowRepository;
    private final InstituteAdmissionPreferenceRepository preferenceRepository;

    public SeatMatrixServiceImpl(SeatMatrixRepository seatMatrixRepository, ProgrammesOfferedRepository programmesOfferedRepository, AdmissionWindowRepository admissionWindowRepository, InstituteAdmissionPreferenceRepository preferenceRepository) {
        this.seatMatrixRepository = seatMatrixRepository;
        this.programmesOfferedRepository = programmesOfferedRepository;
        this.admissionWindowRepository = admissionWindowRepository;
        this.preferenceRepository = preferenceRepository;
    }

    @Override
    public SeatMatrixResponseDTO createOrUpdateSeatMatrix(SeatMatrixRequestDTO request, Short loggedInInstituteId) {
        ProgrammeOffered programmeOffered = programmesOfferedRepository.findById(request.getProgrammeOfferedId())
                .orElseThrow(() -> new RuntimeException("ProgrammeOffered not found"));

        // SECURITY AUDIT LOCK: Verify Ownership (IDOR Prevention)
        Short ownerInstituteId = programmeOffered.getInstituteDepartment().getInstitute().getInstituteId();
        if (!ownerInstituteId.equals(loggedInInstituteId)) {
            throw new SecurityException("Unauthorized: You do not have permission to modify seats for this programme.");
        }

        AdmissionWindow admissionWindow = admissionWindowRepository.findById(request.getAdmissionWindowId())
                .orElseThrow(() -> new RuntimeException("AdmissionWindow not found"));

        SeatMatrix seatMatrix = seatMatrixRepository.findByProgrammeOfferedProgrammeOfferedId(request.getProgrammeOfferedId())
                .orElse(new SeatMatrix());

        // PERMANENT LOCK: Prevent editing if already submitted
        if ("SUBMITTED".equals(seatMatrix.getApprovalStatus())) {
            throw new IllegalStateException("Seat Matrix is locked. You have already finalized and submitted these seats.");
        }

        seatMatrix.setProgrammeOffered(programmeOffered);
        seatMatrix.setAdmissionWindow(admissionWindow);
        seatMatrix.setTotalSeats(request.getTotalSeats());

        // Ensure it stays in DRAFT until they explicitly submit
        seatMatrix.setApprovalStatus("DRAFT");

        seatMatrix = seatMatrixRepository.save(seatMatrix);

        SeatMatrixResponseDTO response = new SeatMatrixResponseDTO();
        response.setId(seatMatrix.getId());
        response.setProgrammeOfferedId(seatMatrix.getProgrammeOffered().getProgrammeOfferedId());
        response.setTotalSeats(seatMatrix.getTotalSeats());
        return response;
    }

    @Override
    public Optional<SeatMatrix> getSeatMatrixByProgrammeOfferedId(Integer programmeOfferedId) {
        return seatMatrixRepository.findByProgrammeOfferedProgrammeOfferedId(programmeOfferedId);
    }

    @Override
    public void deleteSeatMatrix(Long seatMatrixId) {
        seatMatrixRepository.deleteById(seatMatrixId);
    }

    @Override
    public List<SeatMatrixResponseDTO> getByInstitute(Short instituteId) {
        List<ProgrammeOffered> programmes = programmesOfferedRepository
                .findWithAllDetailsByInstituteDepartment_Institute_InstituteId(instituteId);

        List<Integer> programmeIds = programmes.stream()
                .map(ProgrammeOffered::getProgrammeOfferedId)
                .collect(Collectors.toList());

        List<SeatMatrix> matrices = seatMatrixRepository
                .findAllByProgrammeOfferedProgrammeOfferedIdIn(programmeIds);

        return matrices.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private SeatMatrixResponseDTO convertToDTO(SeatMatrix seatMatrix) {
        SeatMatrixResponseDTO dto = new SeatMatrixResponseDTO();
        dto.setId(seatMatrix.getId());
        dto.setProgrammeOfferedId(seatMatrix.getProgrammeOffered().getProgrammeOfferedId());
        dto.setTotalSeats(seatMatrix.getTotalSeats());
        return dto;
    }

    @Override
    public int findSeatsByProgrammeOfferedId(Integer programmeOfferedId) {
        return seatMatrixRepository.findByProgrammeOfferedProgrammeOfferedId(programmeOfferedId)
                .map(SeatMatrix::getTotalSeats)
                .orElse(0);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeatMatrixResponseDTO> getByAdmissionWindow(Short admissionWindowId) {
        List<SeatMatrix> matrices = seatMatrixRepository.findByAdmissionWindowId(admissionWindowId);
        return matrices.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void sendForApproval(Short admissionWindowId, List<Integer> programmeOfferedIds, Short loggedInInstituteId) {
        List<SeatMatrix> matrices = seatMatrixRepository.findByAdmissionWindow_AdmissionIdAndProgrammeOffered_ProgrammeOfferedIdIn(
                admissionWindowId, programmeOfferedIds
        );

        for (SeatMatrix sm : matrices) {

            // SECURITY AUDIT LOCK: Verify Ownership (IDOR Prevention)
            Short ownerInstituteId = sm.getProgrammeOffered().getInstituteDepartment().getInstitute().getInstituteId();
            if (!ownerInstituteId.equals(loggedInInstituteId)) {
                throw new SecurityException("Unauthorized: You do not have permission to submit seats for one or more selected programmes.");
            }

            // Lock the status to SUBMITTED permanently
            if ("DRAFT".equals(sm.getApprovalStatus())) {
                sm.setApprovalStatus("SUBMITTED");
                seatMatrixRepository.save(sm);
            }
        }

        // Lock the CUET preference for this institute+window — cannot be changed after Final Submit
        preferenceRepository
                .findByInstituteInstituteIdAndAdmissionWindowAdmissionId(loggedInInstituteId, admissionWindowId)
                .ifPresent(pref -> {
                    pref.setPreferenceSubmitted(true);
                    preferenceRepository.save(pref);
                });
    }

    // --- DEPRECATED CONTROLLER APPROVAL METHODS ---
    // (Kept empty to fulfill interface contracts without breaking the app,
    // but the Controller will no longer use these)

    @Override
    public List<SeatApprovalRowDTO> getPendingApprovals() {
        return List.of();
    }

    @Override
    public List<SeatApprovalRowDTO> getAllSeatApprovals() {
        // If you still want the Controller to view submitted seats read-only,
        // you can change this to query for "SUBMITTED" status.
        List<SeatMatrix> submittedList = seatMatrixRepository.findByApprovalStatus("SUBMITTED");
        return submittedList.stream().map(this::mapToApprovalDTO).collect(Collectors.toList());
    }

    private SeatApprovalRowDTO mapToApprovalDTO(SeatMatrix sm) {
        SeatApprovalRowDTO dto = new SeatApprovalRowDTO();
        dto.setSeatMatrixId(sm.getId());
        dto.setProgrammeOfferedId(sm.getProgrammeOffered().getProgrammeOfferedId());
        dto.setTotalSeats(sm.getTotalSeats());
        dto.setStatus(sm.getApprovalStatus());

        var po = sm.getProgrammeOffered();
        if(po != null) {
            if(po.getProgramme() != null) {
                dto.setProgrammeName(po.getProgramme().getProgrammeName());
                if(po.getProgramme().getStream() != null) {
                    dto.setStreamName(po.getProgramme().getStream().getStreamName());
                }
            }
            if(po.getInstituteDepartment() != null && po.getInstituteDepartment().getInstitute() != null) {
                dto.setInstituteName(po.getInstituteDepartment().getInstitute().getInstituteName());
            }
        }
        return dto;
    }

    @Override
    public void approveSeatMatrix(Long id) {}

    @Override
    public void rejectSeatMatrix(Long id, String reason) {}
}