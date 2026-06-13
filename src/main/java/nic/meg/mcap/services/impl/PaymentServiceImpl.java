package nic.meg.mcap.services.impl;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import nic.meg.mcap.entities.Payment;
import nic.meg.mcap.repositories.PaymentRepository;
import nic.meg.mcap.services.PaymentService;

@Service
public class PaymentServiceImpl implements PaymentService {

    private static final String BASE_URL = "https://api.razorpay.com/v1/orders";

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private PaymentRepository paymentRepository;

    // ─────────────────────────────────────────────────────────────────────
    // 1. CREATE ORDER — called before showing the Razorpay checkout modal
    // ─────────────────────────────────────────────────────────────────────
    @Override
    public Map<String, String> createOrder(Double amount, String customerId, String customerName,
                                           String customerEmail, String customerPhone,
                                           String returnUrl, String orderId) {

        // Razorpay requires amount in PAISE (multiply by 100, no decimals)
        int amountInPaise = (int) Math.round(amount * 100);

        Map<String, Object> request = Map.of(
            "amount",   amountInPaise,
            "currency", "INR",
            "receipt",  orderId,
            "notes", Map.of(
                "customer_id",    customerId,
                "customer_name",  customerName,
                "customer_email", customerEmail
            )
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, getHeaders());
        ResponseEntity<Map> response = restTemplate.postForEntity(BASE_URL, entity, Map.class);

        Map body = response.getBody();
        if (body == null || !body.containsKey("id")) {
            throw new RuntimeException("Invalid response from Razorpay: " + body);
        }

        String razorpayOrderId = (String) body.get("id");

        // Save to DB — paymentSessionId field now stores the Razorpay order id
        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setPaymentSessionId(razorpayOrderId);
        payment.setAmount(amount);
        payment.setCurrency("INR");
        payment.setCustomerId(customerId);
        payment.setStatus("CREATED");
        payment.setCreatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        // Return keyId + razorpayOrderId — frontend needs both to open the modal
        return Map.of(
            "orderId",   razorpayOrderId,
            "keyId",     keyId,
            "receiptId", orderId
        );
    }

    // ─────────────────────────────────────────────────────────────────────
    // 2. FETCH PAYMENT STATUS
    //    Razorpay order statuses: "created" | "attempted" | "paid"
    // ─────────────────────────────────────────────────────────────────────
    @Override
    public Map<String, Object> fetchPaymentStatus(String razorpayOrderId) {
        String url = BASE_URL + "/" + razorpayOrderId;
        HttpEntity<Void> entity = new HttpEntity<>(getHeaders());
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
        Map<String, Object> body = response.getBody();

        if (body != null && body.containsKey("status")) {
            String rzpStatus = (String) body.get("status");
            String internalStatus = switch (rzpStatus) {
                case "paid"      -> "PAYMENT_SUCCESS";
                case "attempted" -> "PAYMENT_ATTEMPTED";
                default          -> rzpStatus.toUpperCase();
            };
            paymentRepository.findByPaymentSessionId(razorpayOrderId)
                .ifPresent(p -> updatePaymentStatus(p.getOrderId(), internalStatus));
        }

        return body;
    }

    // ─────────────────────────────────────────────────────────────────────
    // 3. VERIFY PAYMENT SIGNATURE
    //    HMAC-SHA256(keySecret, razorpayOrderId + "|" + razorpayPaymentId)
    // ─────────────────────────────────────────────────────────────────────
    @Override
    public boolean verifyPaymentSignature(String razorpayOrderId,
                                          String razorpayPaymentId,
                                          String razorpaySignature) {
        try {
            String message = razorpayOrderId + "|" + razorpayPaymentId;
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            mac.init(new javax.crypto.spec.SecretKeySpec(
                keySecret.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(message.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) hexString.append(String.format("%02x", b));
            return hexString.toString().equals(razorpaySignature);
        } catch (Exception e) {
            throw new RuntimeException("Signature verification failed", e);
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 4. UPDATE PAYMENT STATUS — gateway-agnostic, no changes needed
    // ─────────────────────────────────────────────────────────────────────
    @Override
    public void updatePaymentStatus(String orderId, String status) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        payment.setStatus(status);
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);
    }

    // ─────────────────────────────────────────────────────────────────────
    // HEADERS — Basic Auth replaces x-client-id / x-client-secret
    // ─────────────────────────────────────────────────────────────────────
    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(keyId, keySecret);
        return headers;
    }
}
