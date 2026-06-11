package nic.meg.mcap.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import nic.meg.mcap.entities.AffiliationType;

public interface AffiliationTypeRepository extends JpaRepository<AffiliationType, Integer> {
    
}

