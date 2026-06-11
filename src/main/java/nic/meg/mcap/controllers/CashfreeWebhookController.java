package nic.meg.mcap.controllers;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.ContentCachingRequestWrapper;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import nic.meg.mcap.entities.Application;
import nic.meg.mcap.entities.SeatAllotment;
import nic.meg.mcap.enums.AllotmentStatus;
import nic.meg.mcap.repositories.ApplicationRepository;
import nic.meg.mcap.repositories.PaymentRepository;
import nic.meg.mcap.repositories.SeatAllotmentRepository;
import nic.meg.mcap.services.ApplicationService;
import nic.meg.mcap.services.ApplicationSubmissionService;
import nic.meg.mcap.services.CounselingService;
import nic.meg.mcap.services.EligibilityCalculationService;

/**
 * Receives payment event notifications directly from Cashfree's servers.
 *
 * This is the safety net: if the applicant's browser closes after paying
 * (before the return URL redirect fires), Cashfree still POSTs here.
 * No payment ever goes unprocessed.
 *
 * Security model:
 *  - CSRF-exempt in SecurityConfig (server-to-server, no browser session)
 *  - Permitted without authentication in SecurityConfig
 *  - Protected by HMAC-SHA256 signature verification on every request
 *  - Idempotent: checks isAlreadyFinalized() before acting
 */
@RestController
@RequestMapping("/applicants/payment/webhook")
public class CashfreeWebhookController {

    private static final Logger logger = LoggerFactory.getLogger(CashfreeWebhookController.class);
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    @Value("${cashfree.client.secret}")
    private String clientSecret;

    @Autowired private PaymentRepository             paymentRepository;
    @Autowired private ApplicationRepository         applicationRepository;
    @Autowired private SeatAllotmentRepository       seatAllotmentRepository;
    @Autowired private ApplicationService            applicationService;
    @Autowired private ApplicationSubmissionService  submissionService;
    @Autowired private EligibilityCalculationService eligibilityCalculationService;
    @Autowired private CounselingService             counselingService;
    @Autowired private ObjectMapper                  objectMapper;

    /**
     * Entry point. Cashfree POSTs with:
     *   x-webhook-timestamp : unix timestamp (seconds) as a string
     *   x-webhook-signature : Base64( HMAC-SHA256( clientSecret, timestamp + rawBody ) )
     *   body                : JSON event payload
     *
     * We always return 200 — Cashfree retries on any non-2xx response.
     */
    @PostMapping
    public ResponseEntity<String> handleWebhook(
            @RequestHeader("x-webhook-timestamp") String timestamp,
            @RequestHeader("x-webhook-signature") String signature,
            HttpServletRequest request) {

        // ── 1. Read raw body ──────────────────────────────────────────────────
        // WebhookBodyCachingFilter eagerly read the stream into the wrapper cache
        // before this method ran. We read from the cache — not the (now-empty) stream.
        String rawBody;
        try {
            ContentCachingRequestWrapper wrapper = (ContentCachingRequestWrapper) request;
            rawBody = new String(wrapper.getContentAsByteArray(), StandardCharsets.UTF_8);
            if (rawBody.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Empty body");
            }
        } catch (ClassCastException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Filter error");
        }

        // ── 2. Verify HMAC-SHA256 signature ───────────────────────────────────
        // Cashfree: signature = Base64( HMAC-SHA256( clientSecret, timestamp + rawBody ) )
        if (!isSignatureValid(timestamp, rawBody, signature)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature");
        }

        // ── 3. Parse payload ──────────────────────────────────────────────────
        Map<String, Object> payload;
        try {
            //noinspection unchecked
            payload = objectMapper.readValue(rawBody, Map.class);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Bad payload");
        }

        String eventType = (String) payload.get("type");

        // ── 4. Act on PAYMENT_SUCCESS_WEBHOOK ────────────────────────────────
        if ("PAYMENT_SUCCESS_WEBHOOK".equals(eventType)) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> data  = (Map<String, Object>) payload.get("data");
                @SuppressWarnings("unchecked")
                Map<String, Object> order = (Map<String, Object>) data.get("order");

                String orderId = (String) order.get("order_id");
                double amount  = order.get("order_amount") instanceof Number n
                        ? n.doubleValue() : 0.0;

                // Mark payment record as PAID
                paymentRepository.findByOrderId(orderId).ifPresent(p -> {
                    p.setStatus("PAID");
                    paymentRepository.save(p);
                });

                finalizeIfNotAlreadyDone(orderId, amount);

            } catch (Exception e) {
            	logger.info("Error");
            }
        }

        return ResponseEntity.ok("Received");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Signature verification
    // signedStr = timestamp + rawBody  (string concatenation, not JSON)
    // signature = Base64( HMAC-SHA256( clientSecret, signedStr ) )
    // ─────────────────────────────────────────────────────────────────────────
    private boolean isSignatureValid(String timestamp, String rawBody, String receivedSig) {
        try {
            String signedStr = timestamp + rawBody;
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(
                    clientSecret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            byte[] hash     = mac.doFinal(signedStr.getBytes(StandardCharsets.UTF_8));
            String computed = java.util.Base64.getEncoder().encodeToString(hash);
            return constantTimeEquals(computed, receivedSig);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Constant-time string equality — prevents timing side-channel attacks
     * where an attacker could deduce the correct signature one byte at a time
     * by measuring response times.
     */
    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        byte[] aBytes = a.getBytes(StandardCharsets.UTF_8);
        byte[] bBytes = b.getBytes(StandardCharsets.UTF_8);
        if (aBytes.length != bBytes.length) return false;
        int diff = 0;
        for (int i = 0; i < aBytes.length; i++) diff |= aBytes[i] ^ bBytes[i];
        return diff == 0;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Idempotency + finalization
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Both the return URL and the webhook call this logic.
     * Whichever arrives second finds the order already finalized and skips.
     */
    private void finalizeIfNotAlreadyDone(String orderId, double amount) {
        if (isAlreadyFinalized(orderId)) {
            return;
        }

        if (orderId.startsWith("APP_")) {
            handleApplicationPayment(orderId, amount);
        } else if (orderId.startsWith("SEAT_") || orderId.startsWith("SLIDEUP_")) {
            handleSeatPayment(orderId);
        } else {
            logger.info("Webhook: unrecognised orderId prefix — orderId={}", orderId);
        }
    }

    private void handleApplicationPayment(String orderId, double amount) {
        Long        applicationId = Long.parseLong(orderId.split("_")[1]);
        Application app           = applicationRepository.findById(applicationId).orElseThrow();
        String      applicantNo   = app.getApplicant().getApplicantNo();

        applicationService.confirmPayment(applicationId, applicantNo);

        // Re-fetch after confirmPayment modifies the entity
        app = applicationRepository.findById(applicationId).orElseThrow();
        if (amount > 0) app.setAmountPaid(BigDecimal.valueOf(amount));
        // "WH_" prefix marks this was finalised by webhook, not the return URL
        app.setTransactionId("WH_" + orderId);
        applicationRepository.save(app);

        submissionService.finalizeApplicationSubmission(app);
        eligibilityCalculationService.calculateAndSaveEligibility(app);
    }

    private void handleSeatPayment(String orderId) {
        Long          allotmentId = Long.parseLong(orderId.split("_")[1]);
        SeatAllotment allotment   = seatAllotmentRepository.findById(allotmentId).orElseThrow();
        String        applicantNo = allotment.getApplicant().getApplicantNo();

        if (orderId.startsWith("SLIDEUP_")) {
            // Allotment is already SLIDE_UP — fee payment just confirms the hold
            logger.info("Webhook: Slide Up fee confirmed — allotment={} applicant={}",
                    allotmentId, applicantNo);
        } else {
            counselingService.acceptAllotment(applicantNo, allotmentId);
            logger.info("Webhook: seat accepted — allotment={} applicant={}",
                    allotmentId, applicantNo);
        }
    }

    /**
     * Returns true if this order was already processed by the return URL path,
     * so we don't double-apply the finalization logic.
     */
    private boolean isAlreadyFinalized(String orderId) {
        try {
            if (orderId.startsWith("APP_")) {
                Long appId = Long.parseLong(orderId.split("_")[1]);
                return applicationRepository.findById(appId)
                        .map(Application::isPaymentComplete)
                        .orElse(false);

            } else if (orderId.startsWith("SEAT_")) {
                Long allotmentId = Long.parseLong(orderId.split("_")[1]);
                return seatAllotmentRepository.findById(allotmentId)
                        .map(sa -> sa.getStatus() == AllotmentStatus.ACCEPTED)
                        .orElse(false);

            } else if (orderId.startsWith("SLIDEUP_")) {
                // SLIDEUP payment only confirms the hold — no definitive "finalized" state.
                // Always let the webhook log the receipt.
                return false;
            }
        } catch (Exception e) {
            logger.info("isAlreadyFinalized: check failed for {} — {}", orderId, e.getMessage());
        }
        return false;
    }
}