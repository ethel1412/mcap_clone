
package nic.meg.mcap.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import nic.meg.mcap.entities.Relationship;

@Repository
public interface RelationshipRepository extends JpaRepository<Relationship, Short> {
}
