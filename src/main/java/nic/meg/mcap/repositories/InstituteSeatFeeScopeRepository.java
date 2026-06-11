package nic.meg.mcap.repositories;

import nic.meg.mcap.entities.InstituteSeatFeeScope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InstituteSeatFeeScopeRepository extends JpaRepository<InstituteSeatFeeScope, Long> {
    List<InstituteSeatFeeScope> findByFeeStructure_FeeStructureId(Long feeStructureId);
}
