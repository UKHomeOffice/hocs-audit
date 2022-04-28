package uk.gov.digital.ho.hocs.audit.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.audit.repository.entity.AuditEvent;
import uk.gov.digital.ho.hocs.audit.repository.entity.CaseReference;

import javax.persistence.QueryHint;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.hibernate.annotations.QueryHints.READ_ONLY;
import static org.hibernate.jpa.QueryHints.HINT_CACHEABLE;
import static org.hibernate.jpa.QueryHints.HINT_FETCH_SIZE;

@Repository
public interface AuditRepositoryLatestEvents {

    @QueryHints(value = {
            @QueryHint(name = HINT_FETCH_SIZE, value = "1"),
            @QueryHint(name = HINT_CACHEABLE, value = "false"),
            @QueryHint(name = READ_ONLY, value = "true")
    })
    @Query(value = "SELECT a.* FROM audit_event_latest_events a WHERE a.audit_timestamp between ?1 and 'tomorrow' AND a.type in ?2 AND a.case_type = ?3 AND a.deleted = false ORDER BY a.case_uuid, a.type, a.audit_timestamp DESC", nativeQuery = true)
    Stream<AuditEvent> findAuditEventLatestEventsAfterDate(LocalDateTime of, String[] events, String caseType);

    @QueryHints(value = {
            @QueryHint(name = HINT_FETCH_SIZE, value = "1"),
            @QueryHint(name = HINT_CACHEABLE, value = "false"),
            @QueryHint(name = READ_ONLY, value = "true")
    })
    @Query(value = "SELECT audit_payload->>'reference' AS caseReference, cast(case_uuid AS VARCHAR(36)) as caseUuid FROM audit_event_latest_events WHERE type = 'CASE_CREATED' AND case_type = ?1", nativeQuery = true)
    Stream<CaseReference> getCaseReferencesForType(String caseType);

}
