package nic.meg.mcap.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import nic.meg.mcap.entities.PageUrl;

public interface PageUrlRepository extends JpaRepository<PageUrl, Short> {
}
