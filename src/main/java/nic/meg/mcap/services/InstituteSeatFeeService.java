package nic.meg.mcap.services;

import nic.meg.mcap.dto.request.InstituteSeatFeeStructureRequestDTO;
import nic.meg.mcap.dto.response.InstituteSeatFeeStructureResponseDTO;

import java.math.BigDecimal;
import java.util.List;

public interface InstituteSeatFeeService {

    /** Return all active fee structures for the institute identified by userId */
    List<InstituteSeatFeeStructureResponseDTO> getStructuresByUserId(Integer userId);

    /** Create a new fee structure */
    InstituteSeatFeeStructureResponseDTO createStructure(Integer userId, InstituteSeatFeeStructureRequestDTO dto);

    /** Update an existing fee structure (replaces particulars and scopes) */
    InstituteSeatFeeStructureResponseDTO updateStructure(Long feeStructureId, Integer userId, InstituteSeatFeeStructureRequestDTO dto);

    /** Soft-delete a fee structure */
    void deleteStructure(Long feeStructureId, Integer userId);

    /** Get a single structure with full detail */
    InstituteSeatFeeStructureResponseDTO getStructureById(Long feeStructureId, Integer userId);

    /**
     * Resolve the seat-acceptance fee total for a specific programmeOffered.
     * Checks: direct programme scope first; falls back to stream scope.
     * Returns null if no fee structure is configured for this programme.
     */
    BigDecimal resolveAcceptanceFee(Integer programmeOfferedId);

    /** Full DTO for the fee structure applicable to a programme (for applicant view). */
    InstituteSeatFeeStructureResponseDTO resolveAcceptanceFeeStructure(Integer programmeOfferedId);
}
