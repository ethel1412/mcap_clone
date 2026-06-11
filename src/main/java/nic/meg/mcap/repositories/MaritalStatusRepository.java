package nic.meg.mcap.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import nic.meg.mcap.entities.MaritalStatus;

@Repository
public interface MaritalStatusRepository extends JpaRepository<MaritalStatus, String> {
}
