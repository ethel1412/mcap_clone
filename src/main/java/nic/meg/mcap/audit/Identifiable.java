package nic.meg.mcap.audit;

/**
 * Marker interface for JPA entities that participate in audit logging.
 *
 * <p>Implementing this interface allows {@link AuditingEntityListener} to extract
 * the entity's primary key without using reflection, eliminating the Fortify
 * "Access Specifier Manipulation" finding caused by {@code field.setAccessible(true)}.
 *
 * <p><b>Usage:</b> Add {@code implements Identifiable} to any {@code @Entity} class
 * and implement {@code getAuditId()} to return the primary key as a String:
 *
 * <pre>{@code
 * @Entity
 * public class Institute implements Identifiable {
 *     @Id
 *     private Short instituteId;
 *
 *     @Override
 *     public String getAuditId() {
 *         return instituteId != null ? instituteId.toString() : null;
 *     }
 * }
 * }</pre>
 *
 * <p>Entities that do not yet implement this interface will still be audited via
 * the safe {@code MethodHandles} fallback in {@link AuditingEntityListener}.
 */
public interface Identifiable {

    /**
     * Returns the entity's primary key as a String for audit logging purposes.
     *
     * @return the primary key value, or {@code null} if not yet assigned
     */
    String getAuditId();
}