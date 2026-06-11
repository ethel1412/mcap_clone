package nic.meg.mcap.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import nic.meg.mcap.entities.InstituteRegistrationFee;
import nic.meg.mcap.enums.Caste;

@Repository
public interface InstituteRegistrationFeeRepository extends JpaRepository<InstituteRegistrationFee, Integer> {

    List<InstituteRegistrationFee> findByUserUserIdAndIsActiveTrue(Integer userId);

    Optional<InstituteRegistrationFee> findByUserUserIdAndCasteAndIsActiveTrue(Integer userId, Caste caste);

    boolean existsByUserUserIdAndCaste(Integer userId, Caste caste);
}