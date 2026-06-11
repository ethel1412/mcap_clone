package nic.meg.mcap.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import nic.meg.mcap.entities.Gender;

@Repository
public interface GenderRepository extends JpaRepository<Gender, String> {
}
