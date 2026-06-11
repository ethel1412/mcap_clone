package nic.meg.mcap.repositories;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import nic.meg.mcap.entities.OtpToken;
import nic.meg.mcap.enums.OtpPurpose;
import nic.meg.mcap.enums.OtpStatus;

public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {

	Optional<OtpToken> findTopByIdentifierAndPurposeAndStatusOrderByCreatedAtDesc(String identifier, OtpPurpose purpose,
			OtpStatus status);

	long countByIdentifierAndPurposeAndCreatedAtAfter(String identifier, OtpPurpose purpose, Instant after);

	List<OtpToken> findByIdentifierAndPurposeAndStatus(String identifier, OtpPurpose purpose, OtpStatus status);

	long deleteByStatusAndExpiresAtBefore(OtpStatus status, Instant threshold);

	Optional<OtpToken> findTopByIdentifierAndPurposeOrderByCreatedAtDesc(String identifier, OtpPurpose registration);
}
