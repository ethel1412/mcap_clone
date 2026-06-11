package nic.meg.mcap.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import nic.meg.mcap.entities.Address;

public interface AddressRepository extends JpaRepository<Address, Integer> {
    Optional<Address> findByEntityIdAndAddressType(UUID entityId, String addressType);
    List<Address> findByEntityId(UUID entityId);
}