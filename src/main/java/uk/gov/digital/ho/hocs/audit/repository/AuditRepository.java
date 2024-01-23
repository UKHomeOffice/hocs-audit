package uk.gov.digital.ho.hocs.audit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.audit.repository.entity.AuditEvent;
import uk.gov.digital.ho.hocs.audit.repository.entity.CaseReference;

import jakarta.persistence.QueryHint;
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
    @Query(value = "SELECT a.* FROM audit_event a WHERE a.audit_timestamp BETWEEN ?1 AND ?2 AND a.type in ?3 AND a.deleted = false ORDER BY a.audit_timestamp ASC",
           nativeQuery = true)
    Stream<AuditEvent> findAuditDataByDateRangeAndEvents(LocalDateTime dateFrom,
                                                         LocalDateTime dateTo,
                                                         String[] types);

    @QueryHints(value = { @QueryHint(name = HINT_FETCH_SIZE, value = "5000"),
        @QueryHint(name = HINT_CACHEABLE, value = "false"), @QueryHint(name = READ_ONLY, value = "true") })
    @Query(value = "SELECT DISTINCT ON (case_uuid, type) a.* FROM audit_event a WHERE a.audit_timestamp BETWEEN ?1 AND ?2 AND a.type in ?3 AND a.case_type = ?4 AND a.deleted = false ORDER BY a.case_uuid, a.type, a.audit_timestamp DESC;",
           nativeQuery = true)
    Stream<AuditEvent> findLastAuditDataByDateRangeAndEvents(LocalDateTime dateFrom,
                                                             LocalDateTime dateTo,
                                                             String[] types,
                                                             String caseType);

    @QueryHints(value = {
        @QueryHint(name = HINT_FETCH_SIZE, value = "5000"),
        @QueryHint(name = HINT_CACHEABLE, value = "false"),
        @QueryHint(name = READ_ONLY, value = "true")
    })
    @Query(value = """
        WITH
        -- If we know the event is unique we can skip distinct/ordering per case
        created_messages AS (
            SELECT *
            FROM audit_event
            WHERE
                audit_timestamp BETWEEN ?1 AND ?2
              AND type IN ?4
              AND case_type = ?3
              AND deleted = FALSE
        ),
        -- limit to events before applying ordering
        range_messages AS (
        SELECT DISTINCT ON (case_uuid, type) case_uuid, type FROM audit.audit_event
          WHERE audit_timestamp BETWEEN ?1 AND ?2
          AND type IN ?5
          AND case_type = ?3
          AND deleted = FALSE
        ),
        latest_updated_completed_messages AS (
            SELECT e.*
            FROM range_messages rm
            CROSS JOIN LATERAL (
                SELECT *
                FROM audit.audit_event
                WHERE audit_timestamp BETWEEN ?1 AND ?2
                  AND type IN ?5
                  AND case_type = ?3
                  AND deleted = FALSE
                  AND case_uuid = rm.case_uuid
                  AND type = rm.type
                ORDER BY audit_timestamp DESC
                LIMIT 1
            ) e
        )
        SELECT c.* FROM created_messages c
        UNION
        SELECT u.* FROM latest_updated_completed_messages u
        ORDER BY case_uuid, type, audit_timestamp DESC;
        """, nativeQuery = true)
    Stream<AuditEvent> findLastAuditDataByDateRangeAndEvents(
        LocalDateTime dateFrom,
        LocalDateTime dateTo,
        String caseType,
        String[] uniqueEventTypes,
        String[] duplicatedEventTypes);

    @Query(value = "SELECT a.* FROM audit_event a WHERE a.case_uuid = ?1", nativeQuery = true)
    List<AuditEvent> findAuditDataByCaseUUID(UUID caseUUID);

    @QueryHints(value = { @QueryHint(name = HINT_FETCH_SIZE, value = "5000"),
        @QueryHint(name = HINT_CACHEABLE, value = "false"), @QueryHint(name = READ_ONLY, value = "true") })
    @Query(value = "SELECT audit_payload->>'reference' AS caseReference, cast(case_uuid AS VARCHAR(36)) as caseUuid FROM audit_event_latest_events WHERE type = 'CASE_CREATED' AND case_type = ?1",
           nativeQuery = true)
    Stream<CaseReference> getCaseReferencesForType(String caseType);

}
