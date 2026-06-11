package nic.meg.mcap.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import nic.meg.mcap.dto.request.ApplicantDTO;
import nic.meg.mcap.dto.request.RegistrationFormDTO;

public class PhoneNumberValidator implements ConstraintValidator<ValidPhoneNumber, Object> {

    @Override
    public boolean isValid(Object dto, ConstraintValidatorContext context) {
        if (dto == null) {
            return true;
        }

        String phoneCode;
        String phoneNumber;

        // Step 1: Check the type of the DTO and get the phone fields
        if (dto instanceof RegistrationFormDTO) {
            RegistrationFormDTO formDTO = (RegistrationFormDTO) dto;
            phoneCode = formDTO.getCountryPhoneCode();
            phoneNumber = formDTO.getPhoneNumber();
        } else if (dto instanceof ApplicantDTO) {
            ApplicantDTO appDTO = (ApplicantDTO) dto;
            phoneCode = appDTO.getCountryPhoneCode();
            phoneNumber = appDTO.getPhoneNumber();
        } else {
            // If the annotation is on a class we don't recognize, ignore it.
            return true;
        }

        // Step 2: Now run your original validation logic
        if (phoneCode == null || phoneNumber == null) {
            return true; // Let @NotEmpty handle this
        }

        if ("+91".equals(phoneCode)) {
            // For India, must be exactly 10 digits
            return phoneNumber.matches("^[0-9]{10}$");
        } else {
            // For other countries, 5 to 15 digits
            return phoneNumber.matches("^[0-9]{5,15}$");
        }
    }
}