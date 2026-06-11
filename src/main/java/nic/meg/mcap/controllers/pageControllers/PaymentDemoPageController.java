package nic.meg.mcap.controllers.pageControllers;

import java.math.BigDecimal;
import java.util.List;

import nic.meg.mcap.entities.Applicant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import nic.meg.mcap.dto.response.ApplicationStatusResponseDTO;
import nic.meg.mcap.dto.response.ProgrammePreferenceResponseDTO;
import nic.meg.mcap.entities.Application;
import nic.meg.mcap.entities.Applicant;
import nic.meg.mcap.repositories.ApplicationRepository;
import nic.meg.mcap.services.ApplicationService;
import nic.meg.mcap.services.ProgrammePreferenceService;

@Controller
@RequestMapping("/applicants/payment")
public class PaymentDemoPageController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentDemoPageController.class);

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private ProgrammePreferenceService preferenceService;

    @Autowired
    private ApplicationService applicationService;

//    @Autowired
//    private Applicant applicant;

    @GetMapping("/show-details")
    public String getPaymentDetailsFragment(@RequestParam("applicationId") Long applicationId, Model model, Authentication auth) {

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found: " + applicationId));

        Applicant applicant = application.getApplicant();

        // --- NEW FLAT ADMISSION FEE LOGIC ---
        boolean isLocal = Boolean.TRUE.equals(applicant.getHasDomicileCertificate());
        String category = applicant.getCommunityCategory() != null ?
                applicant.getCommunityCategory().getCategoryCode().trim().toUpperCase() : "GEN";

        BigDecimal totalFee;

        if (!isLocal) {
            totalFee = new BigDecimal("1000.00"); // Outside State
        } else if ("ST".equals(category) || "SC".equals(category)) {
            totalFee = new BigDecimal("200.00");  // Local SC / ST
        } else {
            totalFee = new BigDecimal("500.00");  // Local OBC / General
        }

        model.addAttribute("payableAmount", totalFee);
        model.addAttribute("applicationId", applicationId);

        ApplicationStatusResponseDTO status = applicationService.getApplicationStatus(applicationId, auth.getName());
        model.addAttribute("status", status);
        model.addAttribute("isPaymentComplete", application.isPaymentComplete());

        return "applicant/fragments/payment-details";
    }
}