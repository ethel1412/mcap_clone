package nic.meg.mcap.dto.request;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateOrderRequestDTO {

	private String order_id;
	private Double order_amount;
	private String order_currency;
	private CustomerDTO customer_details;

	// ✅ add this field
	private Map<String, String> order_meta;
}