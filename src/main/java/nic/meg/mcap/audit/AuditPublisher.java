package nic.meg.mcap.audit;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class AuditPublisher {

	private final ApplicationEventPublisher publisher;

	public AuditPublisher(ApplicationEventPublisher publisher) {
		this.publisher = publisher;
	}

	public void publish(AuditTable audit) {
		publisher.publishEvent(new AuditEvent(audit));
	}
}