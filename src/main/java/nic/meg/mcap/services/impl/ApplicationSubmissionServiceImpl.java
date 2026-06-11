package nic.meg.mcap.services.impl;

import lombok.RequiredArgsConstructor;
import nic.meg.mcap.entities.Application;
import nic.meg.mcap.audit.AuditingEntityListener;
import nic.meg.mcap.entities.Applicant;
import nic.meg.mcap.enums.ApplicantType;
import nic.meg.mcap.repositories.ApplicationRepository;
import nic.meg.mcap.services.ApplicationSubmissionService;
import nic.meg.mcap.services.ApplicationSubmittedEvent;
import nic.meg.mcap.services.EligibilityCalculationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ApplicationSubmissionServiceImpl implements ApplicationSubmissionService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationEventPublisher eventPublisher;

    // Inject the eligibility service right here
    private final EligibilityCalculationService eligibilityCalculationService;
    private static final Logger logger = LoggerFactory.getLogger(ApplicationSubmissionServiceImpl.class);
    

    @Override
    @Transactional
    public void finalizeApplicationSubmission(Application app) {

        // 1. Determine Applicant Type
        Applicant applicant = app.getApplicant();
        boolean hasEntranceScore = (applicant.getCuetScore() != null && applicant.getCuetScore().getApplicationNumber() != null) ||
                (applicant.getJeeScore() != null && applicant.getJeeScore().getApplicationNumber() != null);

        app.setApplicantType(hasEntranceScore ? ApplicantType.WITH_ENTRANCE : ApplicantType.WITHOUT_ENTRANCE);

        // 2. Update Statuses
        app.setPaymentComplete(true);
        app.setApplicationStatus("SUBMITTED");

        applicationRepository.save(app);

        // 3. Fire the Background Event
        eventPublisher.publishEvent(new ApplicationSubmittedEvent(this, app.getApplicationId()));
    }

    // ========================================================================
    // THE BACKGROUND LISTENER IS RIGHT HERE IN THE SAME FILE!
    // ========================================================================

    @Async // Runs safely in the background
    @EventListener
    public void handleApplicationSubmitted(ApplicationSubmittedEvent event) {

        // Fetch fresh from DB to ensure we have the latest state
        Application app = applicationRepository.findById(event.getApplicationId()).orElse(null);

        // Enforce the rule: Only check if fully submitted and paid
        if (app != null && app.isPaymentComplete() && "SUBMITTED".equals(app.getApplicationStatus())) {
            try {
                eligibilityCalculationService.calculateAndSaveEligibility(app);
            } catch (Exception e) {
            	logger.info("Error occurred while processing request");
            }
        }
    }
}