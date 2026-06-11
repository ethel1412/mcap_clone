//package nic.meg.mcap.services;
//
//import nic.meg.mcap.entities.Applicant;
//import nic.meg.mcap.entities.Application;
//import nic.meg.mcap.entities.CommunityCategory;
//import nic.meg.mcap.entities.User;
//import nic.meg.mcap.enums.Caste;
//import nic.meg.mcap.enums.OrgOwnerType; // <-- IMPORT THIS ENUM
//import nic.meg.mcap.dto.response.ProgrammePreferenceResponseDTO;
//import nic.meg.mcap.repositories.UserRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
//import java.math.BigDecimal;
//import java.util.Comparator;
//import java.util.List;
//import java.util.Optional;
//
//@Service
//public class PaymentDemoService {
//
////    @Value("${fees.category.GENERAL}")
//    private BigDecimal defaultFee;
//
//    @Autowired
//    private ProgrammePreferenceService programmePreferenceService;
//
//    @Autowired
//    private InstituteRegistrationFeeService instituteRegistrationFeeService;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    public BigDecimal getFeeForApplication(Application application) {
//        Applicant applicant = application.getApplicant();
//
//        // 1. Find the applicant's first-preference institute ID
//        List<ProgrammePreferenceResponseDTO> preferences = programmePreferenceService.getPreferencesByApplicationId(application.getApplicationId(), applicant.getApplicantNo());
//        Optional<ProgrammePreferenceResponseDTO> firstPreference = preferences.stream()
//                .min(Comparator.comparing(ProgrammePreferenceResponseDTO::getPreferenceOrder));
//
//        if (firstPreference.isEmpty()) {
//            System.out.println("DEBUG: No preferences found. Using default fee.");
//            return defaultFee;
//        }
//        Short instituteId = firstPreference.get().getInstitute().getInstituteId();
//
//        // ======================= THE CORRECT REVERSE LOOKUP =======================
//        // Find the User whose 'orgOwnerType' is INSTITUTE and 'orgOwnerId' matches the instituteId.
//        Optional<User> instituteUserOpt = userRepository.findByOrgOwnerTypeAndOrgOwnerId(OrgOwnerType.INSTITUTE, instituteId);
//        // ========================================================================
//
//        if (instituteUserOpt.isEmpty()) {
//            System.out.println("DEBUG: No user is configured for institute ID " + instituteId + ". Using default fee.");
//            return defaultFee;
//        }
//        Integer instituteUserId = instituteUserOpt.get().getUserId();
//
//        // 3. Determine the applicant's caste and get the fee for that user
//        CommunityCategory category = applicant.getCommunityCategory();
//        if (category == null || category.getCategoryCode() == null) {
//            System.out.println("DEBUG: Applicant community category is null. Using GENERAL fee for institute.");
//            return getInstituteFeeForCaste(instituteUserId, Caste.GENERAL);
//        }
//
//        try {
//            String categoryCode = category.getCategoryCode().trim().toUpperCase();
//            Caste applicantCaste = Caste.valueOf(categoryCode);
//            return getInstituteFeeForCaste(instituteUserId, applicantCaste);
//        } catch (IllegalArgumentException e) {
//            System.out.println("DEBUG: Applicant category code '" + category.getCategoryCode() + "' does not match Caste enum. Using GENERAL fee.");
//            return getInstituteFeeForCaste(instituteUserId, Caste.GENERAL);
//        }
//    }
//
//    private BigDecimal getInstituteFeeForCaste(Integer userId, Caste caste) {
//        return instituteRegistrationFeeService.getFeeByUserIdAndCaste(userId, caste)
//                .map(fee -> BigDecimal.valueOf(fee.getAmount()))
//                .orElse(defaultFee);
//    }
//}