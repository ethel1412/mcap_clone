package nic.meg.mcap.audit;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.persistence.Id;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;

public class AuditingEntityListener {

	private static AuditPublisher auditPublisher;
	private static final Logger logger = LoggerFactory.getLogger(AuditingEntityListener.class);

	public static void setAuditPublisher(AuditPublisher publisher) {
		auditPublisher = publisher;
	}

	// ✅ Safe ObjectMapper
	private static final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule())
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS).addMixIn(Object.class, IgnoreHibernate.class);

	// ✅ Ignore Hibernate proxies
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	private static class IgnoreHibernate {
	}

	@PostPersist
	public void postPersist(Object entity) {
		processAudit(entity, "CREATE");
	}

	@PostRemove
	public void postRemove(Object entity) {
		processAudit(entity, "DELETE");
	}

	@PostUpdate
	public void postUpdate(Object entity) {
		processAudit(entity, "UPDATE");
	}

	private void processAudit(Object entity, String action) {
		try {
			if (auditPublisher == null) {
				return; // ❗ Don't break main flow
			}

			String entityId = getEntityId(entity);
			String json = toSafeJson(entity);

			AuditTable audit = AuditTable.builder().entityName(entity.getClass().getSimpleName()).entityId(entityId)
					.action(action).changedBy("system").timestamp(LocalDateTime.now()).newValue(json).build();

			auditPublisher.publish(audit);

		} catch (Exception e) {
			// ❗ NEVER crash business logic because of audit
			logger.info(e.getMessage());
		}
	}

	// ✅ SAFE JSON (no recursion risk)
	private String toSafeJson(Object entity) {
		try {
			// 👉 Only serialize basic fields (NO relations)
			Map<String, Object> safeMap = new HashMap<>();

			for (Field field : entity.getClass().getDeclaredFields()) {
				field.setAccessible(true);

				// skip relations (avoid recursion)
				if (field.getType().getPackageName().startsWith("nic.meg.mcap.entities")) {
					continue;
				}

				Object value = field.get(entity);
				safeMap.put(field.getName(), value);
			}

			return mapper.writeValueAsString(safeMap);

		} catch (Exception e) {
			return "{}"; // fallback
		}
	}

	private String getEntityId(Object entity) {
		try {
			for (Field field : entity.getClass().getDeclaredFields()) {
				if (field.isAnnotationPresent(Id.class)) {
					field.setAccessible(true);
					Object value = field.get(entity);
					return value != null ? value.toString() : null;
				}
			}
		} catch (Exception e) {
			logger.info(e.getMessage());
		}
		return null;
	}
}