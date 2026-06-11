package nic.meg.mcap.services;

import java.util.Map;

public interface PaymentService {

    // Updated to take dynamic order and customer details
    Map<String, String> createOrder(Double amount, String customerId, String customerName,
                                    String customerEmail, String customerPhone,
                                    String returnUrl, String orderId);

    Map<String, Object> fetchPaymentStatus(String orderId);

    Map<String, Object> payOrder(String sessionId);

    void updatePaymentStatus(String orderId, String status);
}