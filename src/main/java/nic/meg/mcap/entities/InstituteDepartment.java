package nic.meg.mcap.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Size; 
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InstituteDepartment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer instituteDepartmentId;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "institute_id", nullable = false)
	private Institute institute;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "department_id", nullable = false)
	private Department department;

	@Column(nullable = false)
	private boolean active = true;

	// Optional metadata
	@Size(max = 120)
	@Column(length = 120)
	private String hodName;

	@Size(max = 120)
	@Column(length = 120)
	private String email;

	@Size(max = 20)
	@Column(length = 20)
	private String phone;
}
