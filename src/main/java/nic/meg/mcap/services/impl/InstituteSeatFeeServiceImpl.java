package nic.meg.mcap.services.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import nic.meg.mcap.dto.request.InstituteSeatFeeStructureRequestDTO;
import nic.meg.mcap.dto.request.SeatFeeParticularDTO;
import nic.meg.mcap.dto.response.InstituteSeatFeeStructureResponseDTO;
import nic.meg.mcap.dto.response.SeatFeeParticularResponseDTO;
import nic.meg.mcap.dto.response.SeatFeeScopeResponseDTO;
import nic.meg.mcap.entities.*;
import nic.meg.mcap.repositories.*;
import nic.meg.mcap.services.InstituteSeatFeeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InstituteSeatFeeServiceImpl implements InstituteSeatFeeService {

    private final InstituteSeatFeeStructureRepository structureRepo;
    private final ProgrammesOfferedRepository programmeOfferedRepo;
    private final StreamRepository streamRepo;
    private final UserRepository userRepo;

    @Override
    @Transactional(readOnly = true)
    public List<InstituteSeatFeeStructureResponseDTO> getStructuresByUserId(Integer userId) {
        return structureRepo.findByUser_UserIdAndActiveTrue(userId)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public InstituteSeatFeeStructureResponseDTO createStructure(Integer userId,
                                                                InstituteSeatFeeStructureRequestDTO dto) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

        InstituteSeatFeeStructure structure = new InstituteSeatFeeStructure();
        structure.setUser(user);
        structure.setFeeName(dto.getFeeName());

        applyParticulars(structure, dto.getParticulars());
        applyScopes(structure, dto.getProgrammeOfferedIds(), dto.getStreamIds());

        return toResponseDTO(structureRepo.save(structure));
    }

    @Override
    @Transactional
    public InstituteSeatFeeStructureResponseDTO updateStructure(Long feeStructureId, Integer userId,
                                                                InstituteSeatFeeStructureRequestDTO dto) {
        InstituteSeatFeeStructure structure = structureRepo
                .findByFeeStructureIdAndUser_UserId(feeStructureId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Fee structure not found: " + feeStructureId));

        structure.setFeeName(dto.getFeeName());
        structure.getParticulars().clear();
        structure.getScopes().clear();

        applyParticulars(structure, dto.getParticulars());
        applyScopes(structure, dto.getProgrammeOfferedIds(), dto.getStreamIds());

        return toResponseDTO(structureRepo.save(structure));
    }

    @Override
    @Transactional
    public void deleteStructure(Long feeStructureId, Integer userId) {
        InstituteSeatFeeStructure structure = structureRepo
                .findByFeeStructureIdAndUser_UserId(feeStructureId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Fee structure not found: " + feeStructureId));
        structure.setActive(false);
        structureRepo.save(structure);
    }

    @Override
    @Transactional(readOnly = true)
    public InstituteSeatFeeStructureResponseDTO getStructureById(Long feeStructureId, Integer userId) {
        return structureRepo.findByFeeStructureIdAndUser_UserId(feeStructureId, userId)
                .map(this::toResponseDTO)
                .orElseThrow(() -> new EntityNotFoundException("Fee structure not found: " + feeStructureId));
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal resolveAcceptanceFee(Integer programmeOfferedId) {
        InstituteSeatFeeStructureResponseDTO dto = resolveAcceptanceFeeStructure(programmeOfferedId);
        return dto != null ? dto.getTotalAmount() : null;
    }

    @Override
    @Transactional(readOnly = true)
    public InstituteSeatFeeStructureResponseDTO resolveAcceptanceFeeStructure(Integer programmeOfferedId) {
        List<InstituteSeatFeeStructure> candidates =
                structureRepo.findApplicableStructures(programmeOfferedId);
        if (candidates.isEmpty()) return null;
        // Prefer most-specific (programme-level) over stream-level
        // findApplicableStructures returns programme-scope match first if present
        return toResponseDTO(candidates.get(0));
    }

    // ---- helpers ----

    private void applyParticulars(InstituteSeatFeeStructure structure, List<SeatFeeParticularDTO> dtoList) {
        if (dtoList == null) return;
        int order = 0;
        for (SeatFeeParticularDTO p : dtoList) {
            InstituteSeatFeeParticular particular = new InstituteSeatFeeParticular();
            particular.setFeeStructure(structure);
            particular.setParticularName(p.getParticularName());
            particular.setAmount(p.getAmount());
            particular.setDisplayOrder(order++);
            structure.getParticulars().add(particular);
        }
    }

    private void applyScopes(InstituteSeatFeeStructure structure,
                             List<Integer> programmeOfferedIds, List<Short> streamIds) {
        List<InstituteSeatFeeScope> scopes = new ArrayList<>();

        if (programmeOfferedIds != null) {
            for (Integer poId : programmeOfferedIds) {
                ProgrammeOffered po = programmeOfferedRepo.findById(poId)
                        .orElseThrow(() -> new EntityNotFoundException("ProgrammeOffered not found: " + poId));
                InstituteSeatFeeScope scope = new InstituteSeatFeeScope();
                scope.setFeeStructure(structure);
                scope.setProgrammeOffered(po);
                scopes.add(scope);
            }
        }

        if (streamIds != null) {
            for (Short sId : streamIds) {
                Stream stream = streamRepo.findById(sId)
                        .orElseThrow(() -> new EntityNotFoundException("Stream not found: " + sId));
                InstituteSeatFeeScope scope = new InstituteSeatFeeScope();
                scope.setFeeStructure(structure);
                scope.setStream(stream);
                scopes.add(scope);
            }
        }

        structure.getScopes().addAll(scopes);
    }

    private InstituteSeatFeeStructureResponseDTO toResponseDTO(InstituteSeatFeeStructure s) {
        List<SeatFeeParticularResponseDTO> particulars = s.getParticulars().stream()
                .map(p -> SeatFeeParticularResponseDTO.builder()
                        .particularId(p.getParticularId())
                        .particularName(p.getParticularName())
                        .amount(p.getAmount())
                        .displayOrder(p.getDisplayOrder())
                        .build())
                .collect(Collectors.toList());

        BigDecimal total = s.getParticulars().stream()
                .map(InstituteSeatFeeParticular::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<SeatFeeScopeResponseDTO> scopes = s.getScopes().stream()
                .map(sc -> {
                    if (sc.getProgrammeOffered() != null) {
                        return SeatFeeScopeResponseDTO.builder()
                                .scopeId(sc.getScopeId())
                                .scopeType("PROGRAMME")
                                .programmeOfferedId(sc.getProgrammeOffered().getProgrammeOfferedId())
                                .programmeName(sc.getProgrammeOffered().getProgramme().getProgrammeName())
                                .build();
                    } else {
                        return SeatFeeScopeResponseDTO.builder()
                                .scopeId(sc.getScopeId())
                                .scopeType("STREAM")
                                .streamId(sc.getStream().getStreamId())
                                .streamName(sc.getStream().getStreamName())
                                .build();
                    }
                })
                .collect(Collectors.toList());

        String scopeSummary = scopes.stream()
                .map(sc -> "STREAM".equals(sc.getScopeType())
                        ? "[Stream] " + sc.getStreamName()
                        : sc.getProgrammeName())
                .collect(Collectors.joining(", "));

        return InstituteSeatFeeStructureResponseDTO.builder()
                .feeStructureId(s.getFeeStructureId())
                .feeName(s.getFeeName())
                .totalAmount(total)
                .particulars(particulars)
                .scopes(scopes)
                .scopeSummary(scopeSummary.isEmpty() ? "—" : scopeSummary)
                .build();
    }
}
