package nic.meg.mcap.repositories;

import nic.meg.mcap.entities.InstituteSeatFeeParticular;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InstituteSeatFeeParticularRepository extends JpaRepository<InstituteSeatFeeParticular, Long> {
    List<InstituteSeatFeeParticular> findByFeeStructure_FeeStructureIdOrderByDisplayOrderAsc(Long feeStructureId);
}
