package nic.meg.mcap.services;

import java.util.List;
import java.util.Optional;

import nic.meg.mcap.dto.request.InstituteRegistrationFeeRequestDTO;
import nic.meg.mcap.dto.response.InstituteRegistrationFeeResponseDTO;
import nic.meg.mcap.enums.Caste;

public interface InstituteRegistrationFeeService {

    InstituteRegistrationFeeResponseDTO saveFee(Integer userId, InstituteRegistrationFeeRequestDTO feeDTO);

    List<InstituteRegistrationFeeResponseDTO> getFeesByUserId(Integer userId);

    Optional<InstituteRegistrationFeeResponseDTO> getFeeByUserIdAndCaste(Integer userId, Caste caste);

    InstituteRegistrationFeeResponseDTO updateFee(Integer feeId, InstituteRegistrationFeeRequestDTO feeDTO);

    void deleteFee(Integer feeId);

    boolean isFeeCategoryExists(Integer userId, Caste caste);
}
