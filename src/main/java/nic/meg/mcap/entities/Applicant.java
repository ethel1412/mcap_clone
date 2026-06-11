package nic.meg.mcap.entities;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import nic.meg.mcap.audit.AuditingEntityListener;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString

public class Applicant {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID applicantId;

	@Column(name = "last_selected_application_id")
	private Long lastSelectedApplicationId;

	@Column(unique = true, nullable = false, length = 50)
	private String applicantNo;

//    @Convert(converter = StringCryptoConverter.class)
	@Column(nullable = false, length = 255)
	private String firstName;

//    @Convert(converter = StringCryptoConverter.class)
	@Column(length = 255)
	private String middleName;

//    @Convert(converter = StringCryptoConverter.class)
	@Column(nullable = false, length = 255)
	private String lastName;

	@Column(name = "country_phone_code", length = 15)
	private String countryPhoneCode;

//    @Convert(converter = StringCryptoConverter.class)
	@Column(nullable = false, length = 255)
	private String phoneNumber;

	@Column(nullable = false, length = 8)
	private LocalDate dateOfBirth;

//    @Convert(converter = StringCryptoConverter.class)
	@Column(nullable = false, length = 255)
	private String email;

	@ManyToOne
	@JoinColumn(name = "religionCode")
	private Religion religion;

	@ManyToOne
	@JoinColumn(name = "statusCode")
	private MaritalStatus maritalStatus;

	@ManyToOne
	@JoinColumn(name = "countryCode")
	private Country countryOfOrigin;

	@ManyToOne
	@JoinColumn(name = "categoryCode", columnDefinition = "CHAR(3)")
	private CommunityCategory communityCategory;

	@ManyToOne
	@JoinColumn(name = "genderCode")
	private Gender gender;

//    @Convert(converter = StringCryptoConverter.class)
	@Column(length = 255)
	private String fatherName;

//    @Convert(converter = StringCryptoConverter.class)
	@Column(length = 255)
	private String motherName;

	@Column(length = 4)
	private String guardianPrefix;

//    @Convert(converter = StringCryptoConverter.class)
	@Column(length = 255)
	private String guardianName;

	@Column
	private Boolean hasDomicileCertificate;

	@Column
	private Boolean isDifferentlyAbled;

	@Column
	private Boolean hasNccCertificate;

	@Column
	private Boolean hasNssCertificate;

	@Column
	private Boolean hasBackwardAreaCertificate;

	@Column
	private Boolean hasAnyOtherRelevantCertificate;

	@ManyToOne
	@JoinColumn(name = "guardianRelationshipCode", referencedColumnName = "relationshipCode")
	private Relationship guardianRelationship;

	@OneToMany(mappedBy = "applicant", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	private List<Application> applications = new ArrayList<>();

	@OneToOne(mappedBy = "applicant", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private JeeScore jeeScore;

	@OneToOne(mappedBy = "applicant", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private CuetScore cuetScore;

	@OneToOne(mappedBy = "applicant", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private NetScore netScore;

	@OneToOne(mappedBy = "applicant", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private GateScore gateScore;

	@Column
	private Boolean hasJeeScore;

	@Column
	private Boolean hasCuetScore;

	@Column
	private Boolean hasNetScore;

	@Column
	private Boolean hasGateScore;

	@OneToOne
	@JoinColumn(name = "user_id")
	private User user;
}