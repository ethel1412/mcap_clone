package nic.meg.mcap.audit;

import org.springframework.context.ApplicationEvent;

public class AuditEvent extends ApplicationEvent {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private final AuditTable audit;

	public AuditEvent(AuditTable audit) {
		super(audit); // you must call super
		this.audit = audit;
	}

	public AuditTable getAudit() {
		return audit;
	}
}
