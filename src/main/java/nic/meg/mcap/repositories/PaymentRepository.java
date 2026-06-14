package nic.meg.mcap.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import nic.meg.mcap.entities.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(String orderId);
    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId); // replaces findByPaymentSessionId
}
