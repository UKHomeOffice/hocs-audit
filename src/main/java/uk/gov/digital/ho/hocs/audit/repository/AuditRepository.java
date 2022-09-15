package uk.gov.digital.ho.hocs.audit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.audit.repository.entity.AuditEvent;
import uk.gov.digital.ho.hocs.audit.repository.entity.CaseReference;

import javax.persistence.QueryHint;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.hibernate.annotations.QueryHints.READ_ONLY;
import static org.hibernate.jpa.QueryHints.HINT_CACHEABLE;
import static org.hibernate.jpa.QueryHints.HINT_FETCH_SIZE;

@Repository
public interface AuditRepository extends JpaRepository<AuditEvent, String>, AuditRepositoryCustom {

    @QueryHints(value = { @QueryHint(name = READ_ONLY, value = "true") })
    @Query(value = "SELECT a.* FROM audit_event a WHERE a.audit_timestamp < 'tomorrow' AND a.case_uuid = ?1 AND a.type IN ?2 AND a.deleted = false",
           nativeQuery = true)
    List<AuditEvent> findAuditDataByCaseUUIDAndTypesIn(UUID caseUUID, String[] types);

    @QueryHints(value = { @QueryHint(name = READ_ONLY, value = "true") })
    @Query(value = "SELECT a.* FROM audit_event a WHERE a.audit_timestamp BETWEEN ?3 AND 'tomorrow' AND a.case_uuid = ?1 AND a.type IN ?2 AND a.deleted = false",
           nativeQuery = true)
    List<AuditEvent> findAuditDataByCaseUUIDAndTypesInAndFrom(UUID caseUUID, String[] types, LocalDate from);

    @QueryHints(value = { @QueryHint(name = HINT_FETCH_SIZE, value = "5000"),
        @QueryHint(name = HINT_CACHEABLE, value = "false"), @QueryHint(name = READ_ONLY, value = "true") })
    @Query(value = "SELECT a.* FROM audit_event a WHERE a.audit_timestamp BETWEEN ?1 AND ?2 AND a.type in ?3 AND a.case_type = ?4 AND a.deleted = false ORDER BY a.audit_timestamp ASC",
           nativeQuery = true)
    Stream<AuditEvent> findAuditDataByDateRangeAndEvents(LocalDateTime dateFrom,
                                                         LocalDateTime dateTo,
                                                         String[] types,
                                                         String caseType);

    @QueryHints(value = { @QueryHint(name = HINT_FETCH_SIZE, value = "5000"),
        @QueryHint(name = HINT_CACHEABLE, value = "false"), @QueryHint(name = READ_ONLY, value = "true") })
    @Query(value = "SELECT DISTINCT ON (case_uuid, type) a.* FROM audit_event a WHERE a.audit_timestamp BETWEEN ?1 AND ?2 AND a.type in ?3 AND a.case_type = ?4 AND a.deleted = false ORDER BY a.case_uuid, a.type, a.audit_timestamp DESC;",
           nativeQuery = true)
    Stream<AuditEvent> findLastAuditDataByDateRangeAndEvents(LocalDateTime dateFrom,
                                                             LocalDateTime dateTo,
                                                             String[] types,
                                                             String caseType);

    @Query(value = "SELECT a.* FROM audit_event a WHERE a.case_uuid = ?1", nativeQuery = true)
    List<AuditEvent> findAuditDataByCaseUUID(UUID caseUUID);

    @QueryHints(value = { @QueryHint(name = HINT_FETCH_SIZE, value = "5000"),
        @QueryHint(name = HINT_CACHEABLE, value = "false"), @QueryHint(name = READ_ONLY, value = "true") })
    @Query(value = "SELECT a.* FROM audit_event_latest_events a " + "WHERE a.audit_timestamp BETWEEN ?1 AND ?2 AND " + "a.type IN ?3 AND " + "a.case_type = ?4 AND " + "a.deleted = false ORDER BY a.case_uuid, a.type, a.audit_timestamp DESC " + "FOR UPDATE",
           nativeQuery = true)
    Stream<AuditEvent> findAuditEventLatestEventsAfterDate(LocalDateTime of,
                                                           LocalDateTime to,
                                                           String[] events,
                                                           String caseType);

    @QueryHints(value = { @QueryHint(name = HINT_FETCH_SIZE, value = "5000"),
        @QueryHint(name = HINT_CACHEABLE, value = "false"), @QueryHint(name = READ_ONLY, value = "true") })
    @Query(value = "SELECT audit_payload->>'reference' AS caseReference, cast(case_uuid AS VARCHAR(36)) as caseUuid FROM audit_event_latest_events WHERE type = 'CASE_CREATED' AND case_type = ?1",
           nativeQuery = true)
    Stream<CaseReference> getCaseReferencesForType(String caseType);

}
