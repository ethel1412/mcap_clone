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

    /** Meghalaya state code as stored in the state table */
    private static final short MEGHALAYA_STATE_CODE = 17;

    @GetMapping("/make-payment")
    public String showPaymentPage(@RequestParam(required = false) String sessionId,
                                  @RequestParam(required = false) String orderId, Model model) {
        model.addAttribute("paymentSessionId", sessionId);
        model.addAttribute("orderId", orderId);
        return "applicant/payment/make-payment";
    }

    @PostMapping("/initiate-application-fee")
    public String initiateApplicationFee(@RequestParam("applicationId") Long applicationId, Authentication auth,
                                         HttpServletRequest httpRequest, RedirectAttributes redirectAttributes) {
        try {
            Application app = applicationRepository.findById(applicationId)
                    .filter(a -> a.getApplicant().getApplicantNo().equals(auth.getName()))
                    .orElseThrow(() -> new SecurityException("Application not found or unauthorized."));

            Applicant applicant = app.getApplicant();

            // --- FEE LOGIC ---
            // SC/ST residents of Meghalaya (permanent address stateCode = 17) → ₹200
            // Other residents of Meghalaya (OBC/General with domicile certificate) → ₹500
            // Applicants from outside Meghalaya → ₹1000
            boolean hasDomicile = Boolean.TRUE.equals(applicant.getHasDomicileCertificate());
            String category = applicant.getCommunityCategory() != null ?
                    applicant.getCommunityCategory().getCategoryCode().trim().toUpperCase() : "GEN";

            // Check if permanent address is in Meghalaya
            boolean permanentAddressInMeghalaya = addressRepository
                    .findByEntityIdAndAddressType(applicant.getApplicantId(), "PERMANENT")
                    .map(addr -> addr.getState() != null && addr.getState().getStateCode() == MEGHALAYA_STATE_CODE)
                    .orElse(false);

            boolean isMeghalayaResident = hasDomicile || permanentAddressInMeghalaya;

            BigDecimal totalFee;

            if (!isMeghalayaResident) {
                totalFee = new BigDecimal("1000.00"); // Outside Meghalaya
            } else if ("ST".equals(category) || "SC".equals(category)) {
                totalFee = new BigDecimal("200.00");  // SC / ST residents of Meghalaya
            } else {
                totalFee = new BigDecimal("500.00");  // OBC / General residents of Meghalaya
            }

            if (totalFee.compareTo(BigDecimal.ZERO) <= 0) {
                redirectAttributes.addFlashAttribute("errorMessage", "Fee evaluation failed.");
                return "redirect:/applicants/dashboard";
            }

            String orderId = "APP_" + applicationId + "_" + System.currentTimeMillis();

            // FIX 1: Dynamically generate the return URL so the session domain is strictly preserved
            String baseUrl = ServletUriComponentsBuilder.fromRequestUri(httpRequest).replacePath(null).build().toUriString();
            String returnUrl = baseUrl + "/applicants/payment/payment-status?order_id={order_id}";

            String customerName = applicant.getFirstName() + " " + (applicant.getLastName() != null ? applicant.getLastName() : "");
            String customerPhone = applicant.getPhoneNumber() != null ? applicant.getPhoneNumber() : "9999999999";
            String customerEmail = applicant.getEmail() != null ? applicant.getEmail() : "no-email@domain.com";

            Map<String, String> response = paymentService.createOrder(
                    totalFee.doubleValue(), applicant.getApplicantNo(), customerName.trim(),
                    customerEmail, customerPhone, returnUrl, orderId
            );

            return "redirect:/applicants/payment/make-payment?sessionId=" + response.get("sessionId") + "&orderId=" + response.get("orderId");

        } catch (Exception e) {
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

            // Verify status matches the intent
            AllotmentStatus currentStatus = allotment.getStatus();
            boolean validStatus = isSlideUp
                    ? currentStatus == AllotmentStatus.SLIDE_UP
                    : currentStatus == AllotmentStatus.ACCEPTED;

            if (!validStatus) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Allotment is no longer in the correct state for payment.");
                return "redirect:/applicants/dashboard";
            }

            // Resolve dynamic seat acceptance fee from institute's fee structure
            Integer programmeOfferedId = allotment.getProgrammeOffered() != null
                    ? allotment.getProgrammeOffered().getProgrammeOfferedId()
                    : null;

            BigDecimal resolvedFee = (programmeOfferedId != null)
                    ? instituteSeatFeeService.resolveAcceptanceFee(programmeOfferedId)
                    : null;

            if (resolvedFee == null || resolvedFee.compareTo(BigDecimal.ZERO) <= 0) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "No seat acceptance fee has been configured for your allotted programme. Please contact the institute.");
                return "redirect:/applicants/dashboard";
            }

            double acceptanceFee = resolvedFee.doubleValue();

            Applicant applicant = allotment.getApplicant();

            // Prefix encodes the action: SEAT_ = accept, SLIDEUP_ = slide-up hold
            String prefix = isSlideUp ? "SLIDEUP_" : "SEAT_";
            String orderId = prefix + allotmentId + "_" + System.currentTimeMillis();

            String baseUrl = ServletUriComponentsBuilder.fromRequestUri(httpRequest)
                    .replacePath(null).build().toUriString();
            String returnUrl = baseUrl + "/applicants/payment/payment-status?order_id={order_id}";

            String customerName = applicant.getFirstName() + " "
                    + (applicant.getLastName() != null ? applicant.getLastName() : "");
            String customerPhone = applicant.getPhoneNumber() != null ? applicant.getPhoneNumber() : "9999999999";
            String customerEmail = applicant.getEmail() != null ? applicant.getEmail() : "no-email@domain.com";

            Map<String, String> response = paymentService.createOrder(
                    acceptanceFee, applicant.getApplicantNo(), customerName.trim(),
                    customerEmail, customerPhone, returnUrl, orderId);

            return "redirect:/applicants/payment/make-payment?sessionId="
                    + response.get("sessionId") + "&orderId=" + response.get("orderId");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Could not initiate seat fee payment.");
            return "redirect:/applicants/dashboard";
        }
    }

    /**
     * Returns the fee structure breakdown for a given programmeOfferedId.
     * Called via AJAX from the applicant dashboard before the applicant proceeds to pay.
     */
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

    // FIX 2: Remove Authentication requirement from the return URL processing
    @GetMapping("/payment-status")
    public String paymentStatus(@RequestParam("order_id") String orderId, Model model) {

        Map<String, Object> response = paymentService.fetchPaymentStatus(orderId);
        String status = (String) response.get("order_status");

        if ("PAID".equalsIgnoreCase(status) || "SUCCESS".equalsIgnoreCase(status)) {
            finalizeSuccessfulPayment(orderId, response);
        }

        model.addAttribute("orderId", orderId);
        model.addAttribute("status", status);

        return "applicant/payment/payment-status";
    }

    // FIX 3: Look up applicantNo directly from the database to bypass session loss vulnerabilities
    private void finalizeSuccessfulPayment(String orderId, Map<String, Object> cashfreeResponse) {
        try {
            if (orderId.startsWith("APP_")) {
                Long applicationId = Long.parseLong(orderId.split("_")[1]);

                Application app = applicationRepository.findById(applicationId).orElseThrow();
                String applicantNo = app.getApplicant().getApplicantNo();

                applicationService.confirmPayment(applicationId, applicantNo);

                app = applicationRepository.findById(applicationId).orElseThrow();

                if (cashfreeResponse.get("order_amount") != null) {
                    app.setAmountPaid(BigDecimal.valueOf(
                            Double.parseDouble(cashfreeResponse.get("order_amount").toString())));
                }

                String txId = cashfreeResponse.get("cf_order_id") != null
                        ? cashfreeResponse.get("cf_order_id").toString()
                        : cashfreeResponse.getOrDefault("payment_session_id", "").toString();
                app.setTransactionId(txId.length() > 100 ? txId.substring(0, 100) : txId);

                applicationRepository.save(app);
                submissionService.finalizeApplicationSubmission(app);
                eligibilityCalculationService.calculateAndSaveEligibility(app);

            } else if (orderId.startsWith("SEAT_") || orderId.startsWith("SLIDEUP_")) {
                // Both SEAT_ and SLIDEUP_ encode the allotmentId in position [1]
                Long allotmentId = Long.parseLong(orderId.split("_")[1]);
                SeatAllotment allotment = seatAllotmentRepository.findById(allotmentId).orElseThrow();
                String applicantNo = allotment.getApplicant().getApplicantNo();

                if (orderId.startsWith("SLIDEUP_")) {
                    // Payment confirms the slide-up hold — status stays SLIDE_UP (already set)
                    // Just log; the allotment is already in SLIDE_UP state from the earlier button click
                    logger.info("Slide Up fee paid for allotment {} by applicant {}", allotmentId, applicantNo);
                } else {
                    // SEAT_ → standard acceptance
                    counselingService.acceptAllotment(applicantNo, allotmentId);
                    logger.info("Seat acceptance fee paid, allotment {} accepted for applicant {}", allotmentId, applicantNo);
                }
            }
        } catch (Exception e) {
            logger.info("Failed to finalise post-payment logic for order {}", orderId, e);
        }
    }

    @GetMapping("/payment-status-api/{orderId}")
    @ResponseBody
    public Map<String, Object> getPaymentStatusAPI(@PathVariable String orderId) {
        Map<String, Object> response = paymentService.fetchPaymentStatus(orderId);
        String status = (String) response.get("order_status");

        if ("PAID".equalsIgnoreCase(status) || "SUCCESS".equalsIgnoreCase(status)) {
            finalizeSuccessfulPayment(orderId, response);
        }

        return response;
    }

    @GetMapping("/receipt/{id}")
    public org.springframework.http.ResponseEntity<byte[]> downloadReceipt(@PathVariable("id") Long applicationId, Authentication auth) {
        try {
            byte[] pdfBytes = pdfGenerationService.generateReceiptPdf(applicationId, auth.getName());

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_PDF);
            // "inline" opens it in a new browser tab. Change to "attachment" if you want it to force-download.
            headers.setContentDispositionFormData("inline", "Payment-Receipt-" + applicationId + ".pdf");

            return new org.springframework.http.ResponseEntity<>(pdfBytes, headers, org.springframework.http.HttpStatus.OK);

        } catch (IllegalStateException e) {
            return org.springframework.http.ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.internalServerError().build();
        }
    }
}
