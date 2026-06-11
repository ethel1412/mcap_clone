package nic.meg.mcap.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import nic.meg.mcap.entities.District;

@Repository
public interface DistrictRepository extends JpaRepository<District, Short> {
    List<District> findByState_StateCode(Short stateCode);

}