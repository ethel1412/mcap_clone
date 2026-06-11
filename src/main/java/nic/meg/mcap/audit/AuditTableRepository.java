package nic.meg.mcap.audit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditTableRepository extends JpaRepository<AuditTable, Long> {

}
