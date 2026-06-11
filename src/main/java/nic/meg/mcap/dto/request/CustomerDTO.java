package nic.meg.mcap.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerDTO {

	private String customer_id;
	private String customer_name;
	private String customer_email;
	private String customer_phone;

	public CustomerDTO() {
	}

	public CustomerDTO(String customer_id, String customer_name, String customer_email, String customer_phone) {
		this.customer_id = customer_id;
		this.customer_name = customer_name;
		this.customer_email = customer_email;
		this.customer_phone = customer_phone;
	}

}
