package uk.gov.digital.ho.hocs.audit.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.audit.repository.entity.AuditEvent;

import javax.persistence.QueryHint;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Stream;

import static org.hibernate.annotations.QueryHints.READ_ONLY;
import static org.hibernate.jpa.QueryHints.HINT_CACHEABLE;
import static org.hibernate.jpa.QueryHints.HINT_FETCH_SIZE;

@Repository
public interface AuditRepository extends CrudRepository<AuditEvent, String>, AuditRepositoryCustom {

    @QueryHints(value = {
            @QueryHint(name = HINT_FETCH_SIZE, value = "1"),
            @QueryHint(name = HINT_CACHEABLE, value = "false"),
            @QueryHint(name = READ_ONLY, value = "true")
    })
    @Query(value = "SELECT a.* FROM audit_event a WHERE a.audit_timestamp < 'tomorrow' AND a.case_uuid = ?1 AND a.type IN ?2 AND a.deleted = false", nativeQuery = true)
    Stream<AuditEvent> findAuditDataByCaseUUIDAndTypesIn(UUID caseUUID, String[] types);

    @QueryHints(value = {
            @QueryHint(name = HINT_FETCH_SIZE, value = "1"),
            @QueryHint(name = HINT_CACHEABLE, value = "false"),
            @QueryHint(name = READ_ONLY, value = "true")
    })
    @Query(value = "SELECT a.* FROM audit_event a WHERE a.audit_timestamp BETWEEN ?3 AND 'tomorrow' AND a.case_uuid = ?1 AND a.type IN ?2 AND a.deleted = false", nativeQuery = true)
    Stream<AuditEvent> findAuditDataByCaseUUIDAndTypesInAndFrom(UUID caseUUID, String[] types, LocalDate from);

    @Modifying
    @Query(value = "UPDATE audit_event a SET a.deleted = ?2  WHERE a.case_uuid = ?1", nativeQuery = true)
    int updateAuditDataDeleted(UUID caseUUID, boolean deleted);

    @Query(value = "SELECT a.* FROM audit_event a WHERE a.audit_timestamp BETWEEN ?1 AND ?2 AND a.type in ?3 AND a.case_type = ?4 AND a.deleted = false ORDER BY a.audit_timestamp ASC", nativeQuery = true)
    Stream<AuditEvent> findAuditDataByDateRangeAndEvents(LocalDateTime dateFrom, LocalDateTime dateTo, String[] types, String caseType);

    @Query(value = "SELECT DISTINCT ON (case_uuid, type) a.* FROM audit_event a WHERE a.audit_timestamp BETWEEN ?1 AND ?2 AND a.type in ?3 AND a.case_type = ?4 AND a.deleted = false ORDER BY a.case_uuid, a.type, a.audit_timestamp DESC;", nativeQuery = true)
    Stream<AuditEvent> findLastAuditDataByDateRangeAndEvents(LocalDateTime dateFrom, LocalDateTime dateTo, String[] types, String caseType);

    @Query(value = "SELECT a.* FROM audit_event_latest_events a WHERE a.audit_timestamp between ?1 and 'tomorrow' AND a.type in ?2 AND a.case_type = ?3 AND a.deleted = false ORDER BY a.case_uuid, a.type, a.audit_timestamp DESC", nativeQuery = true)
    Stream<AuditEvent> findAuditEventLatestEventsAfterDate(LocalDateTime of, String[] events, String caseType);

}
