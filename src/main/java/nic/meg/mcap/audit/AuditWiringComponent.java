package nic.meg.mcap.audit;

import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuditWiringComponent {

	private final AuditPublisher auditPublisher;

	@PostConstruct
	public void wire() {
		AuditingEntityListener.setAuditPublisher(auditPublisher);
	}
}
