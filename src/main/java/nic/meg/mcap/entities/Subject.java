package nic.meg.mcap.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nic.meg.mcap.enums.SubjectType;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor

public class Subject {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer subjectId;

	@NotBlank
	@Size(max = 150)
    @Column(nullable = false, length = 150)
    private String subjectName;

	@Size(max = 20)
	@Column(length = 20)
	private String subjectCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubjectType subjectType;

    @ManyToMany(mappedBy = "subjects")
    @JsonIgnore
    private Set<Stream> streams = new HashSet<>();
}