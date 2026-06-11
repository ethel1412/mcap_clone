package nic.meg.mcap.entities;

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
@Table(name = "customers")
public class Customer {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true)
	private String orderId; // <-- matches your APP_... logic

	private String name;

	@Column(nullable = false)
	private String email;

	@Column(nullable = false)
	private String phone;

	public Customer(String orderId, String name, String email, String phone) {
		this.orderId = orderId;
		this.name = name;
		this.email = email;
		this.phone = phone;
	}
}