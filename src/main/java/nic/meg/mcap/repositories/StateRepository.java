package nic.meg.mcap.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import nic.meg.mcap.entities.State;

@Repository
public interface StateRepository extends JpaRepository<State, Short> {
}
