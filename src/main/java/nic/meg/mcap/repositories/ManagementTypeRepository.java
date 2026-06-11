package nic.meg.mcap.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import nic.meg.mcap.entities.ManagementType;

public interface ManagementTypeRepository extends JpaRepository<ManagementType, Integer> {
    

}
