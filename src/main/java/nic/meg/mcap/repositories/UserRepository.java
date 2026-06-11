package nic.meg.mcap.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import nic.meg.mcap.entities.User;
import nic.meg.mcap.enums.OrgOwnerType;

public interface UserRepository extends JpaRepository<User, Integer> {
	Optional<User> findByUsername(String username);

	Optional<User> findByUserId(Integer userid);

	Optional<User> findByUserCode(UUID userCode);

	Optional<User> findByUsernameStartsWithIgnoreCase(String username);

	boolean existsByUsername(String username);

	Optional<User> findByOrgOwnerTypeAndOrgOwnerId(OrgOwnerType orgOwnerType, Short orgOwnerId);

}