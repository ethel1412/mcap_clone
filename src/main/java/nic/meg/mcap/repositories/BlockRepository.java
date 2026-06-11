package nic.meg.mcap.repositories;

import java.util.List;
import java.util.Optional;

import nic.meg.mcap.entities.District;
import org.springframework.data.jpa.repository.JpaRepository;

import nic.meg.mcap.entities.Block;

public interface BlockRepository extends JpaRepository<Block, Short> {
    List<Block> findByDistrict_DistrictCode(Short districtCode);
}
