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

import nic.meg.mcap.dto.request.CreateOrderRequestDTO;
import nic.meg.mcap.dto.request.CustomerDTO;
import nic.meg.mcap.entities.Payment;
import nic.meg.mcap.repositories.PaymentRepository;
import nic.meg.mcap.services.PaymentService;

@Service
public class PaymentServiceImpl implements PaymentService {

    private static final String BASE_URL = "https://sandbox.cashfree.com/pg/orders";

    @Value("${cashfree.client.id}")
    private String clientId;

    @Value("${cashfree.client.secret}")
    private String clientSecret;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private PaymentRepository paymentRepository;

    @Override
    public Map<String, String> createOrder(Double amount, String customerId, String customerName,
                                           String customerEmail, String customerPhone,
                                           String returnUrl, String orderId) {

        // 1. Map real applicant details
        CustomerDTO customer = new CustomerDTO(customerId, customerName, customerEmail, customerPhone);

        // 2. Prepare Cashfree Request
        CreateOrderRequestDTO request = new CreateOrderRequestDTO();
        request.setOrder_id(orderId);
        request.setOrder_amount(amount);
        request.setOrder_currency("INR");
        request.setCustomer_details(customer);
        request.setOrder_meta(Map.of("return_url", returnUrl));

        HttpEntity<CreateOrderRequestDTO> entity = new HttpEntity<>(request, getHeaders());

        ResponseEntity<Map> response = restTemplate.postForEntity(BASE_URL, entity, Map.class);

        Map body = response.getBody();

        if (body == null || !body.containsKey("payment_session_id")) {
            throw new RuntimeException("Invalid response from Cashfree");
        }

        String sessionId = (String) body.get("payment_session_id");

        // 3. Save to DB
        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setPaymentSessionId(sessionId);
        payment.setAmount(request.getOrder_amount());
        payment.setCurrency(request.getOrder_currency());
        payment.setCustomerId(customer.getCustomer_id());
        payment.setStatus("CREATED");
        payment.setCreatedAt(LocalDateTime.now());

        paymentRepository.save(payment);

        return Map.of("sessionId", sessionId, "orderId", orderId);
    }

    @Override
    public Map<String, Object> fetchPaymentStatus(String orderId) {
        String url = BASE_URL + "/" + orderId;
        HttpEntity<Void> entity = new HttpEntity<>(getHeaders());
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
        Map<String, Object> body = response.getBody();

        if (body != null && body.containsKey("order_status")) {
            String status = (String) body.get("order_status");
            updatePaymentStatus(orderId, status);
        }

        return body;
    }

    @Override
    public Map<String, Object> payOrder(String sessionId) {
        String url = "https://sandbox.cashfree.com/pg/orders/sessions";
        Map<String, Object> body = Map.of(
                "payment_session_id", sessionId,
                "payment_method", Map.of("upi", Map.of("channel", "link"))
        );
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, getHeaders());
        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
        return response.getBody();
    }

    @Override
    public void updatePaymentStatus(String orderId, String status) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        payment.setStatus(status);
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);
    }

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-client-id", clientId);
        headers.set("x-client-secret", clientSecret);
        headers.set("x-api-version", "2023-08-01"); // Updated to latest stable Cashfree version
        return headers;
    }
}