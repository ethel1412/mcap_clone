package nic.meg.mcap.entities;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor

public class SubjectCombinationHeader {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "programme_id", nullable = false)
	private Programme programme;

	@NotBlank
	@Size(max = 120)
	@Column(name = "combination_name", nullable = false, length = 120)
	private String combinationName;

	@Column(nullable = false)
	private Boolean active = Boolean.TRUE;

	@OneToMany(mappedBy = "combination", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<SubjectCombinationItem> items = new ArrayList<>();
}
