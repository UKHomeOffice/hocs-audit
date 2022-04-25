package uk.gov.digital.ho.hocs.audit.auditdetails.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.audit.auditdetails.model.AuditEvent;

import java.time.LocalDateTime;
import java.util.stream.Stream;

@Repository
public interface AuditRepositoryLatestEvents {

    @Query(value = "SELECT a.* FROM audit_event_latest_events a WHERE a.audit_timestamp between ?1 and 'tomorrow' AND a.type in ?2 AND a.case_type = ?3 AND a.deleted = false ORDER BY a.case_uuid, a.type, a.audit_timestamp DESC", nativeQuery = true)
    Stream<AuditEvent> findAuditEventLatestEventsAfterDate(LocalDateTime of, String[] events, String caseType);

}
