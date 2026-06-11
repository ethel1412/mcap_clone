package nic.meg.mcap.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class PersonalDetailsRequestDTO {
    private String firstName;
    private String middleName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String genderCode;
    private String communityCategoryCode;
    private String countryPhoneCode;
    private String phoneNumber;
    private String email;
    private String maritalStatusCode;
    private String religionCode;
    private Short countryCode;

    private String fatherName;
    private String motherName;
    private String guardianPrefix;
    private String guardianName;
    private Short guardianRelationshipCode;

    private ApplicantAddressRequestDTO permanentAddress;
    private ApplicantAddressRequestDTO communicationAddress;
    private String townVillage;

    // THE FIX: Initialize these to false so they never save as null in the DB!
    private Boolean hasDomicileCertificate = false;
    private Boolean isDifferentlyAbled = false;
    private Boolean hasNccCertificate = false;
    private Boolean hasNssCertificate = false;
    private Boolean hasBackwardAreaCertificate = false;
    private Boolean hasAnyOtherRelevantCertificate = false;
}