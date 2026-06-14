package nic.meg.mcap.controllers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import nic.meg.mcap.dto.response.InstituteSeatFeeStructureResponseDTO;
import nic.meg.mcap.services.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;
import nic.meg.mcap.dto.response.ProgrammePreferenceResponseDTO;
import nic.meg.mcap.entities.Address;
import nic.meg.mcap.entities.Applicant;
import nic.meg.mcap.entities.Application;
import nic.meg.mcap.entities.SeatAllotment;
import nic.meg.mcap.enums.AllotmentStatus;
import nic.meg.mcap.repositories.AddressRepository;
import nic.meg.mcap.repositories.ApplicationRepository;
import nic.meg.mcap.repositories.SeatAllotmentRepository;

@Controller
@RequestMapping("/applicants/payment")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    // ─── TEST OVERRIDE ───────────────────────────────────────────────────────────
    // Set to true to charge ₹1 for all payments (Razorpay test mode).
    // REMOVE or set to false before going to production.
    private static final boolean TEST_OVERRIDE_AMOUNT = true;
    private static final BigDecimal TEST_AMOUNT = new BigDecimal("1.00");
    // ─────────────────────────────────────────────────────────────────────────────

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private SeatAllotmentRepository seatAllotmentRepository;

    @Autowired
    private ProgrammePreferenceService preferenceService;

    @Autowired
    private ApplicationSubmissionService submissionService;

    @Autowired
    private CounselingService counselingService;

    @Autowired
    private EligibilityCalculationService eligibilityCalculationService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private PdfGenerationService pdfGenerationService;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private InstituteSeatFeeService instituteSeatFeeService;

    private static final short MEGHALAYA_STATE_CODE = 17;

    /**
     * Shows the Razorpay checkout page.
     * Receives razorpayOrderId, keyId, and receiptId from the redirect.
     */
    @GetMapping("/make-payment")
    public String showPaymentPage(
            @RequestParam(required = false) String razorpayOrderId,
            @RequestParam(required = false) String keyId,
            @RequestParam(required = false) String receiptId,
            Model model) {
        model.addAttribute("razorpayOrderId", razorpayOrderId);
        model.addAttribute("razorpayKeyId",   keyId);
        model.addAttribute("receiptId",        receiptId);
        return "applicant/payment/make-payment";
    }

    @PostMapping("/initiate-application-fee")
    public String initiateApplicationFee(@RequestParam("applicationId") Long applicationId,
                                         Authentication auth,
                                         HttpServletRequest httpRequest,
                                         RedirectAttributes redirectAttributes) {
        try {
            Application app = applicationRepository.findById(applicationId)
                    .filter(a -> a.getApplicant().getApplicantNo().equals(auth.getName()))
                    .orElseThrow(() -> new SecurityException("Application not found or unauthorized."));

            Applicant applicant = app.getApplicant();

            boolean hasDomicile = Boolean.TRUE.equals(applicant.getHasDomicileCertificate());
            String category = applicant.getCommunityCategory() != null ?
                    applicant.getCommunityCategory().getCategoryCode().trim().toUpperCase() : "GEN";

            boolean permanentAddressInMeghalaya = addressRepository
                    .findByEntityIdAndAddressType(applicant.getApplicantId(), "PERMANENT")
                    .map(addr -> addr.getState() != null && addr.getState().getStateCode() == MEGHALAYA_STATE_CODE)
                    .orElse(false);

            boolean isMeghalayaResident = hasDomicile || permanentAddressInMeghalaya;

            BigDecimal totalFee;
            if (TEST_OVERRIDE_AMOUNT) {
                // TEST MODE: charge ₹1 regardless of category/domicile
                totalFee = TEST_AMOUNT;
            } else if (!isMeghalayaResident) {
                totalFee = new BigDecimal("1000.00");
            } else if ("ST".equals(category) || "SC".equals(category)) {
                totalFee = new BigDecimal("200.00");
            } else {
                totalFee = new BigDecimal("500.00");
            }

            if (totalFee.compareTo(BigDecimal.ZERO) <= 0) {
                redirectAttributes.addFlashAttribute("errorMessage", "Fee evaluation failed.");
                return "redirect:/applicants/dashboard";
            }

            String orderId = "APP_" + applicationId + "_" + System.currentTimeMillis();

            String baseUrl = ServletUriComponentsBuilder.fromRequestUri(httpRequest)
                    .replacePath(null).build().toUriString();
            // returnUrl kept for reference — actual redirect is handled by the Razorpay handler callback
            String returnUrl = baseUrl + "/applicants/payment/payment-callback";

            String customerName  = applicant.getFirstName() + " " + (applicant.getLastName() != null ? applicant.getLastName() : "");
            String customerPhone = applicant.getPhoneNumber() != null ? applicant.getPhoneNumber() : "9999999999";
            String customerEmail = applicant.getEmail() != null ? applicant.getEmail() : "no-email@domain.com";

            Map<String, String> response = paymentService.createOrder(
                    totalFee.doubleValue(), applicant.getApplicantNo(), customerName.trim(),
                    customerEmail, customerPhone, returnUrl, orderId);

            // Razorpay: redirect with orderId (rzp order id), keyId (public key), receiptId (internal id)
            return "redirect:/applicants/payment/make-payment"
                    + "?razorpayOrderId=" + response.get("orderId")
                    + "&keyId="           + response.get("keyId")
                    + "&receiptId="       + response.get("receiptId");

        } catch (Exception e) {
            logger.error("Error initiating application fee payment", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Could not initiate payment. Please try again.");
            return "redirect:/applicants/dashboard";
        }
    }

    @PostMapping("/initiate-seat-fee")
    public String initiateSeatFee(@RequestParam("allotmentId") Long allotmentId,
                                  @RequestParam(value = "isSlideUp", required = false, defaultValue = "false") boolean isSlideUp,
                                  Authentication auth,
                                  HttpServletRequest httpRequest,
                                  RedirectAttributes redirectAttributes) {
        try {
            SeatAllotment allotment = seatAllotmentRepository.findById(allotmentId)
                    .orElseThrow(() -> new RuntimeException("Allotment not found"));

            if (!allotment.getApplicant().getApplicantNo().equals(auth.getName())) {
                throw new SecurityException("Unauthorized access to allotment.");
            }

            AllotmentStatus currentStatus = allotment.getStatus();
            boolean validStatus = isSlideUp
                    ? currentStatus == AllotmentStatus.SLIDE_UP
                    : currentStatus == AllotmentStatus.ACCEPTED;

            if (!validStatus) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Allotment is no longer in the correct state for payment.");
                return "redirect:/applicants/dashboard";
            }

            Integer programmeOfferedId = allotment.getProgrammeOffered() != null
                    ? allotment.getProgrammeOffered().getProgrammeOfferedId()
                    : null;

            BigDecimal resolvedFee;
            if (TEST_OVERRIDE_AMOUNT) {
                // TEST MODE: charge ₹1 regardless of configured seat fee
                resolvedFee = TEST_AMOUNT;
            } else {
                resolvedFee = (programmeOfferedId != null)
                        ? instituteSeatFeeService.resolveAcceptanceFee(programmeOfferedId)
                        : null;

                if (resolvedFee == null || resolvedFee.compareTo(BigDecimal.ZERO) <= 0) {
                    redirectAttributes.addFlashAttribute("errorMessage",
                            "No seat acceptance fee has been configured for your allotted programme. Please contact the institute.");
                    return "redirect:/applicants/dashboard";
                }
            }

            double acceptanceFee = resolvedFee.doubleValue();
            Applicant applicant = allotment.getApplicant();

            String prefix  = isSlideUp ? "SLIDEUP_" : "SEAT_";
            String orderId = prefix + allotmentId + "_" + System.currentTimeMillis();

            String baseUrl  = ServletUriComponentsBuilder.fromRequestUri(httpRequest)
                    .replacePath(null).build().toUriString();
            String returnUrl = baseUrl + "/applicants/payment/payment-callback";

            String customerName  = applicant.getFirstName() + " " + (applicant.getLastName() != null ? applicant.getLastName() : "");
            String customerPhone = applicant.getPhoneNumber() != null ? applicant.getPhoneNumber() : "9999999999";
            String customerEmail = applicant.getEmail() != null ? applicant.getEmail() : "no-email@domain.com";

            Map<String, String> response = paymentService.createOrder(
                    acceptanceFee, applicant.getApplicantNo(), customerName.trim(),
                    customerEmail, customerPhone, returnUrl, orderId);

            return "redirect:/applicants/payment/make-payment"
                    + "?razorpayOrderId=" + response.get("orderId")
                    + "&keyId="           + response.get("keyId")
                    + "&receiptId="       + response.get("receiptId");

        } catch (Exception e) {
            logger.error("Error initiating seat fee payment", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Could not initiate seat fee payment.");
            return "redirect:/applicants/dashboard";
        }
    }

    /**
     * Razorpay frontend callback — called after the modal closes (success or failure).
     * The Razorpay JS handler POSTs the three fields below to this endpoint.
     * Signature is verified server-side before any business logic runs.
     */
    @PostMapping("/payment-callback")
    public String paymentCallback(
            @RequestParam("razorpay_order_id")   String razorpayOrderId,
            @RequestParam("razorpay_payment_id") String razorpayPaymentId,
            @RequestParam("razorpay_signature")  String razorpaySignature,
            @RequestParam("receipt_id")           String receiptId,
            RedirectAttributes redirectAttributes) {

        boolean valid = paymentService.verifyPaymentSignature(
                razorpayOrderId, razorpayPaymentId, razorpaySignature);

        if (!valid) {
            logger.warn("Invalid Razorpay signature for order {}", razorpayOrderId);
            redirectAttributes.addFlashAttribute("errorMessage", "Payment verification failed. Please contact support.");
            return "redirect:/applicants/dashboard";
        }

        // Mark as success and run downstream business logic
        paymentService.updatePaymentStatus(receiptId, "PAYMENT_SUCCESS");
        finalizeSuccessfulPayment(receiptId, razorpayPaymentId);

        return "redirect:/applicants/payment/payment-status?order_id=" + receiptId
                + "&payment_id=" + razorpayPaymentId;
    }

    @GetMapping("/payment-status")
    public String paymentStatus(@RequestParam("order_id") String orderId,
                                @RequestParam(required = false) String payment_id,
                                Model model) {
        model.addAttribute("orderId",   orderId);
        model.addAttribute("paymentId", payment_id);
        model.addAttribute("status",    "PAYMENT_SUCCESS");
        return "applicant/payment/payment-status";
    }

    @GetMapping("/seat-fee-structure/{programmeOfferedId}")
    @ResponseBody
    public ResponseEntity<?> getSeatFeeStructure(@PathVariable Integer programmeOfferedId) {
        try {
            InstituteSeatFeeStructureResponseDTO structure =
                    instituteSeatFeeService.resolveAcceptanceFeeStructure(programmeOfferedId);
            if (structure == null) {
                return ResponseEntity.ok(Map.of("particulars", List.of()));
            }
            return ResponseEntity.ok(structure);
        } catch (Exception e) {
            logger.error("Error loading seat fee structure for programmeOffered {}", programmeOfferedId, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Could not load fee structure"));
        }
    }

    @GetMapping("/receipt/{id}")
    public org.springframework.http.ResponseEntity<byte[]> downloadReceipt(
            @PathVariable("id") Long applicationId, Authentication auth) {
        try {
            byte[] pdfBytes = pdfGenerationService.generateReceiptPdf(applicationId, auth.getName());
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", "Payment-Receipt-" + applicationId + ".pdf");
            return new org.springframework.http.ResponseEntity<>(pdfBytes, headers, org.springframework.http.HttpStatus.OK);
        } catch (IllegalStateException e) {
            return org.springframework.http.ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.internalServerError().build();
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────────────────────────────

    private void finalizeSuccessfulPayment(String receiptOrderId, String razorpayPaymentId) {
        try {
            if (receiptOrderId.startsWith("APP_")) {
                Long applicationId = Long.parseLong(receiptOrderId.split("_")[1]);
                Application app = applicationRepository.findById(applicationId).orElseThrow();
                String applicantNo = app.getApplicant().getApplicantNo();

                applicationService.confirmPayment(applicationId, applicantNo);

                app = applicationRepository.findById(applicationId).orElseThrow();
                app.setTransactionId(razorpayPaymentId.length() > 100
                        ? razorpayPaymentId.substring(0, 100) : razorpayPaymentId);
                applicationRepository.save(app);

                submissionService.finalizeApplicationSubmission(app);
                eligibilityCalculationService.calculateAndSaveEligibility(app);

            } else if (receiptOrderId.startsWith("SEAT_") || receiptOrderId.startsWith("SLIDEUP_")) {
                Long allotmentId = Long.parseLong(receiptOrderId.split("_")[1]);
                SeatAllotment allotment = seatAllotmentRepository.findById(allotmentId).orElseThrow();
                String applicantNo = allotment.getApplicant().getApplicantNo();

                if (receiptOrderId.startsWith("SLIDEUP_")) {
                    logger.info("Slide Up fee paid for allotment {} by applicant {}", allotmentId, applicantNo);
                } else {
                    counselingService.acceptAllotment(applicantNo, allotmentId);
                    logger.info("Seat acceptance fee paid, allotment {} accepted for applicant {}", allotmentId, applicantNo);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to finalise post-payment logic for order {}", receiptOrderId, e);
        }
    }
}
