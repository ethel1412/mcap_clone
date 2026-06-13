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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import nic.meg.mcap.entities.Application;
import nic.meg.mcap.entities.SeatAllotment;
import nic.meg.mcap.repositories.ApplicationRepository;
import nic.meg.mcap.repositories.SeatAllotmentRepository;
import nic.meg.mcap.services.ApplicationService;
import nic.meg.mcap.services.ApplicationSubmissionService;
import nic.meg.mcap.services.CounselingService;
import nic.meg.mcap.services.EligibilityCalculationService;
import nic.meg.mcap.services.PaymentService;

/**
 * Handles Razorpay server-to-server webhook events.
 *
 * Configure the webhook URL in Razorpay Dashboard → Settings → Webhooks:
 *   https://yourdomain.com/webhook/razorpay
 *
 * Subscribe to these events:
 *   - payment.captured  (primary success event)
 *   - payment.failed    (for failure tracking)
 *   - order.paid        (optional — fires after all payments on an order complete)
 *
 * This acts as a safety net for cases where the frontend callback (/payment-callback)
 * fails to reach the server (network drop, tab close, etc.).
 */
@RestController
@RequestMapping("/webhook")
public class RazorpayWebhookController {

    private static final Logger logger = LoggerFactory.getLogger(RazorpayWebhookController.class);

    @Value("${razorpay.webhook.secret}")
    private String webhookSecret;

    @Autowired private PaymentService           paymentService;
    @Autowired private ApplicationRepository    applicationRepository;
    @Autowired private SeatAllotmentRepository  seatAllotmentRepository;
    @Autowired private ApplicationService       applicationService;
    @Autowired private ApplicationSubmissionService submissionService;
    @Autowired private EligibilityCalculationService eligibilityCalculationService;
    @Autowired private CounselingService        counselingService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ─────────────────────────────────────────────────────────────────────
    // MAIN WEBHOOK ENDPOINT
    // ─────────────────────────────────────────────────────────────────────
    @PostMapping("/razorpay")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature) {

        // 1. Verify signature — reject unsigned or tampered requests immediately
        if (signature == null || !verifySignature(payload, signature)) {
            logger.warn("Razorpay webhook: invalid or missing signature");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        }

        try {
            Map<String, Object> event = objectMapper.readValue(payload, Map.class);
            String eventType = (String) event.get("event");
            logger.info("Razorpay webhook received: {}", eventType);

            switch (eventType != null ? eventType : "") {
                case "payment.captured" -> handlePaymentCaptured(event);
                case "payment.failed"   -> handlePaymentFailed(event);
                case "order.paid"       -> logger.info("order.paid received — handled via payment.captured");
                default                 -> logger.info("Unhandled webhook event: {}", eventType);
            }

        } catch (Exception e) {
            logger.error("Error processing Razorpay webhook", e);
            // Return 200 even on processing errors so Razorpay does not keep retrying
            return ResponseEntity.ok("Webhook received with processing error");
        }

        return ResponseEntity.ok("OK");
    }

    // ─────────────────────────────────────────────────────────────────────
    // EVENT HANDLERS
    // ─────────────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private void handlePaymentCaptured(Map<String, Object> event) {
        try {
            Map<String, Object> payload    = (Map<String, Object>) event.get("payload");
            Map<String, Object> paymentObj = (Map<String, Object>) payload.get("payment");
            Map<String, Object> entity     = (Map<String, Object>) paymentObj.get("entity");

            String razorpayPaymentId = (String) entity.get("id");
            String razorpayOrderId   = (String) entity.get("order_id");
            // 'notes.receipt' holds your internal orderId (APP_xxx / SEAT_xxx / SLIDEUP_xxx)
            Map<String, Object> notes      = (Map<String, Object>) entity.get("notes");
            // Amount is in paise
            Integer amountPaise = (Integer) entity.get("amount");
            double  amount      = amountPaise != null ? amountPaise / 100.0 : 0.0;

            logger.info("payment.captured: paymentId={}, orderId={}, amount={}" ,
                    razorpayPaymentId, razorpayOrderId, amount);

            // Update gateway-level status
            paymentService.fetchPaymentStatus(razorpayOrderId);

            // Resolve internal receipt id from DB via paymentSessionId (= razorpayOrderId)
            // then run business-logic finalization
            if (notes != null && notes.containsKey("receipt")) {
                String receiptId = (String) notes.get("receipt");
                finalizeSuccessfulPayment(receiptId, razorpayPaymentId, amount);
            }

        } catch (Exception e) {
            logger.error("Error handling payment.captured", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void handlePaymentFailed(Map<String, Object> event) {
        try {
            Map<String, Object> payload    = (Map<String, Object>) event.get("payload");
            Map<String, Object> paymentObj = (Map<String, Object>) payload.get("payment");
            Map<String, Object> entity     = (Map<String, Object>) paymentObj.get("entity");

            String razorpayOrderId = (String) entity.get("order_id");
            logger.warn("payment.failed for razorpay order: {}", razorpayOrderId);

            paymentService.fetchPaymentStatus(razorpayOrderId); // updates status to PAYMENT_ATTEMPTED

        } catch (Exception e) {
            logger.error("Error handling payment.failed", e);
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // BUSINESS LOGIC FINALIZATION (same logic as PaymentController)
    // ─────────────────────────────────────────────────────────────────────

    private void finalizeSuccessfulPayment(String receiptOrderId,
                                           String razorpayPaymentId,
                                           double amount) {
        try {
            paymentService.updatePaymentStatus(receiptOrderId, "PAYMENT_SUCCESS");

            if (receiptOrderId.startsWith("APP_")) {
                Long applicationId = Long.parseLong(receiptOrderId.split("_")[1]);
                Application app    = applicationRepository.findById(applicationId).orElseThrow();
                String applicantNo = app.getApplicant().getApplicantNo();

                applicationService.confirmPayment(applicationId, applicantNo);
                app = applicationRepository.findById(applicationId).orElseThrow();
                app.setAmountPaid(BigDecimal.valueOf(amount));
                app.setTransactionId(razorpayPaymentId.length() > 100
                        ? razorpayPaymentId.substring(0, 100) : razorpayPaymentId);
                applicationRepository.save(app);

                submissionService.finalizeApplicationSubmission(app);
                eligibilityCalculationService.calculateAndSaveEligibility(app);

            } else if (receiptOrderId.startsWith("SEAT_") || receiptOrderId.startsWith("SLIDEUP_")) {
                Long allotmentId   = Long.parseLong(receiptOrderId.split("_")[1]);
                SeatAllotment allot = seatAllotmentRepository.findById(allotmentId).orElseThrow();
                String applicantNo  = allot.getApplicant().getApplicantNo();

                if (receiptOrderId.startsWith("SLIDEUP_")) {
                    logger.info("Webhook: Slide Up fee confirmed for allotment {} by {}", allotmentId, applicantNo);
                } else {
                    counselingService.acceptAllotment(applicantNo, allotmentId);
                    logger.info("Webhook: Seat accepted for allotment {} by {}", allotmentId, applicantNo);
                }
            }
        } catch (Exception e) {
            logger.error("Webhook: Failed to finalize payment for order {}", receiptOrderId, e);
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // SIGNATURE VERIFICATION
    // HMAC-SHA256(webhookSecret, rawBody) must equal X-Razorpay-Signature
    // ─────────────────────────────────────────────────────────────────────
    private boolean verifySignature(String payload, String signature) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format("%02x", b));
            return hex.toString().equals(signature);
        } catch (Exception e) {
            logger.error("Webhook signature verification error", e);
            return false;
        }
    }
}
