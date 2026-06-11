package nic.meg.mcap.entities;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Stream {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Short streamId;

	@Column(name = "stream_name", nullable = false, length = 100)
	private String streamName;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "stream_subjects", joinColumns = @JoinColumn(name = "stream_id"), inverseJoinColumns = @JoinColumn(name = "subject_id"))
	private Set<Subject> subjects = new HashSet<>();
}