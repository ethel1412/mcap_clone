package nic.meg.mcap.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import nic.meg.mcap.entities.Religion;

@Repository
public interface ReligionRepository extends JpaRepository<Religion, String> {
}
