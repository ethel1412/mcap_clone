package nic.meg.mcap.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import nic.meg.mcap.entities.CommunityCategory;

@Repository
public interface CommunityCategoryRepository extends JpaRepository<CommunityCategory, String> {
}
