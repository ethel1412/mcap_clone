package nic.meg.mcap.entities;

import java.sql.Timestamp;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor

public class LoginActivity {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "login_seq")
	@SequenceGenerator(name = "login_seq", sequenceName = "login_sequence", initialValue = 1, allocationSize = 1)

	private Long id;
	private String ipAddress;
	private Boolean isSuccess;

	@CreationTimestamp
	private Timestamp time;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "userId")
	private User user;

	@Column(nullable = true, length = 50)
	private String usernameAttempt;

}