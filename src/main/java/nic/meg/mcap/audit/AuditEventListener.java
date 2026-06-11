package nic.meg.mcap.audit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AuditEventListener {

	@Autowired
	private AuditTableRepository auditRepository;

	@EventListener
	@Transactional(propagation = Propagation.REQUIRES_NEW) // ensures that audit record is saved in a separate independent transaction. This prevents a failure in the audit mechanism from rolling back the main business transaction (e.g., applicant registration).
	public void handleAudit(AuditEvent event) {
		AuditTable audit = event.getAudit();
		auditRepository.save(audit);

	}

}