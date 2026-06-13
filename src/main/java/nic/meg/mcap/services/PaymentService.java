package nic.meg.mcap.services;

import java.util.Map;

public interface PaymentService {

    Map<String, String> createOrder(Double amount, String customerId, String customerName,
                                    String customerEmail, String customerPhone,
                                    String returnUrl, String orderId);

    Map<String, Object> fetchPaymentStatus(String razorpayOrderId);

    boolean verifyPaymentSignature(String razorpayOrderId,
                                   String razorpayPaymentId,
                                   String razorpaySignature);

    void updatePaymentStatus(String orderId, String status);
}
