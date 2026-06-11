package nic.meg.mcap.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import nic.meg.mcap.entities.Country;

@Repository
public interface CountryRepository extends JpaRepository<Country, Short> {
}
