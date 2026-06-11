package nic.meg.mcap.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import nic.meg.mcap.entities.Role;

public interface RoleRepository extends JpaRepository<Role, String> {

    // Find role by its display name
    Optional<Role> findByRoleName(String roleName);

    // Check for duplicate role name
    boolean existsByRoleName(String roleName);
}
