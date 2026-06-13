package nic.meg.mcap.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "payments")
public class Payment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// Your internal order reference (used as Razorpay receipt)
	private String orderId;

	// Razorpay-specific fields
	@Column(name = "razorpay_order_id")
	private String razorpayOrderId;         // e.g. order_XXXXXXXXXXXXXXXX

	@Column(name = "razorpay_payment_id")
	private String razorpayPaymentId;       // e.g. pay_XXXXXXXXXXXXXXXX (set after capture)

	@Column(name = "razorpay_signature")
	private String razorpaySignature;       // HMAC-SHA256 signature (set after payment success)

	private Double amount;
	private String currency;
	private String customerId;
	private String status;                  // CREATED, CAPTURED, FAILED

	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

}
