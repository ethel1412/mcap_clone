package nic.meg.mcap.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.EqualsAndHashCode;

import nic.meg.mcap.entities.Institute; // Keep this import

@Getter
@Setter
@NoArgsConstructor // Keep no-args constructor for flexibility
@AllArgsConstructor // Add all-args if you want to initialize all fields easily
@EqualsAndHashCode(of = "instituteId")
public class InstituteResponseDTO {
    private Short instituteId; // Or other basic info confirming submission
    private String instituteName; // Add this field - this was missing!

    // Keep your existing constructor
    public InstituteResponseDTO(Institute institute) {
        this.instituteId = institute.getInstituteId();
        this.instituteName = institute.getInstituteName(); // Add this line
        // No username/password here as user is not created yet
    }

    // Add a constructor for just the basic info (backward compatibility)
    public InstituteResponseDTO(Short instituteId) {
        this.instituteId = instituteId;
    }
}
