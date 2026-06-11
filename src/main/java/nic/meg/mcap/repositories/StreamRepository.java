package nic.meg.mcap.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import nic.meg.mcap.entities.Stream;

public interface StreamRepository extends JpaRepository<Stream, Short> {
	Optional<Stream> findByStreamName(String streamName);
}
