package nic.meg.mcap.audit;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class AuditTable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String entityName;
	private String entityId;
	private String action; // CREATE, UPDATE, DELETE
	private String changedBy;
	private LocalDateTime timestamp;

	@Lob
	private String oldValue;

	@Lob
	private String newValue;

	// Getters and setters
}
