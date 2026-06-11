package nic.meg.mcap.controllers;

import nic.meg.mcap.entities.Application;
import nic.meg.mcap.repositories.ApplicationRepository;
import nic.meg.mcap.services.ApplicationSubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/applicants/payment")
public class PaymentDemoDataController {

    @Autowired
    private ApplicationRepository applicationRepository;

    // --- INJECT THE NEW SERVICE INTERFACE ---
    @Autowired
    private ApplicationSubmissionService submissionService;

    @PostMapping("/process-demo-payment")
    @ResponseBody
    public ResponseEntity<Map<String, String>> processDemoPayment(
            @RequestParam("applicationId") Long applicationId,
            Authentication auth) throws InterruptedException {

        // 1. Simulate Payment Gateway Delay
        TimeUnit.SECONDS.sleep(2);

        // 2. Security Check & Fetch Application
        String applicantNo = auth.getName();
        Application app = applicationRepository.findById(applicationId)
                .filter(a -> a.getApplicant().getApplicantNo().equals(applicantNo))
                .orElseThrow(() -> new SecurityException("Application not found or unauthorized."));

        // 3. Hand off the heavy lifting to the Submission Service!
        // This will update statuses and trigger the background eligibility check.
        submissionService.finalizeApplicationSubmission(app);

        // 4. Return instant success to the user's browser
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Payment processed successfully!"
        ));
    }
}