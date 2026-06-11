package nic.meg.mcap.services.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import nic.meg.mcap.dto.request.InstituteRegistrationFeeRequestDTO;
import nic.meg.mcap.dto.response.InstituteRegistrationFeeResponseDTO;
import nic.meg.mcap.entities.InstituteRegistrationFee;
import nic.meg.mcap.entities.User;
import nic.meg.mcap.enums.Caste;
import nic.meg.mcap.repositories.InstituteRegistrationFeeRepository;
import nic.meg.mcap.repositories.UserRepository;
import nic.meg.mcap.services.InstituteRegistrationFeeService;

@Service
public class InstituteRegistrationFeeServiceImpl implements InstituteRegistrationFeeService {

    @Autowired
    private InstituteRegistrationFeeRepository feeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    @Transactional
    public InstituteRegistrationFeeResponseDTO saveFee(Integer userId, InstituteRegistrationFeeRequestDTO feeDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // Check if fee already exists for this caste
        Optional<InstituteRegistrationFee> existingFee = feeRepository
                .findByUserUserIdAndCasteAndIsActiveTrue(userId, feeDTO.getCaste());

        InstituteRegistrationFee fee;
        if (existingFee.isPresent()) {
            fee = existingFee.get();
            fee.setAmount(feeDTO.getAmount());
        } else {
            fee = new InstituteRegistrationFee();
            fee.setUser(user);
            fee.setCaste(feeDTO.getCaste());
            fee.setAmount(feeDTO.getAmount());
            fee.setIsActive(true);
        }

        InstituteRegistrationFee savedFee = feeRepository.save(fee);
        return modelMapper.map(savedFee, InstituteRegistrationFeeResponseDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InstituteRegistrationFeeResponseDTO> getFeesByUserId(Integer userId) {
        List<InstituteRegistrationFee> fees = feeRepository.findByUserUserIdAndIsActiveTrue(userId);
        return fees.stream()
                .map(fee -> modelMapper.map(fee, InstituteRegistrationFeeResponseDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<InstituteRegistrationFeeResponseDTO> getFeeByUserIdAndCaste(Integer userId, Caste caste) {
        Optional<InstituteRegistrationFee> fee = feeRepository
                .findByUserUserIdAndCasteAndIsActiveTrue(userId, caste);
        return fee.map(f -> modelMapper.map(f, InstituteRegistrationFeeResponseDTO.class));
    }

    @Override
    @Transactional
    public InstituteRegistrationFeeResponseDTO updateFee(Integer feeId, InstituteRegistrationFeeRequestDTO feeDTO) {
        InstituteRegistrationFee existingFee = feeRepository.findById(feeId)
                .orElseThrow(() -> new RuntimeException("Fee record not found with ID: " + feeId));

        existingFee.setAmount(feeDTO.getAmount());

        InstituteRegistrationFee updatedFee = feeRepository.save(existingFee);
        return modelMapper.map(updatedFee, InstituteRegistrationFeeResponseDTO.class);
    }

    @Override
    @Transactional
    public void deleteFee(Integer feeId) {
        InstituteRegistrationFee existingFee = feeRepository.findById(feeId)
                .orElseThrow(() -> new RuntimeException("Fee record not found with ID: " + feeId));

        existingFee.setIsActive(false);
        feeRepository.save(existingFee);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFeeCategoryExists(Integer userId, Caste caste) {
        return feeRepository.existsByUserUserIdAndCaste(userId, caste);
    }
}

