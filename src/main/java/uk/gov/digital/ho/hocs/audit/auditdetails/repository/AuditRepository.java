package uk.gov.digital.ho.hocs.audit.auditdetails.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import uk.gov.digital.ho.hocs.audit.auditdetails.model.AuditEvent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;


@Repository
public interface AuditRepository extends PagingAndSortingRepository<AuditEvent, String>, AuditRepositoryCustom, AuditRepositoryLatestEvents {

    @Query(value = "SELECT a.* FROM audit_event a WHERE a.case_uuid = ?1 AND a.type IN ?2 AND a.deleted = false", nativeQuery = true)
    List<AuditEvent> findAuditDataByCaseUUIDAndTypesIn(UUID caseUUID, String[] types);

    @Query(value = "SELECT a.* FROM audit_event a WHERE a.case_uuid = ?1 AND a.type IN ?2 AND a.deleted = false AND a.audit_timestamp BETWEEN ?3 AND 'tomorrow'", nativeQuery = true)
    List<AuditEvent> findAuditDataByCaseUUIDAndTypesInAndFrom(UUID caseUUID, String[] types, LocalDate from);

    @Query(value = "SELECT a.* FROM audit_event a WHERE a.audit_timestamp BETWEEN ?1 AND ?2 AND a.type in ?3 AND a.case_type = ?4 AND a.deleted = false ORDER BY a.audit_timestamp ASC", nativeQuery = true)
    Stream<AuditEvent> findAuditDataByDateRangeAndEvents(LocalDateTime dateFrom, LocalDateTime dateTo, String[] types, String caseType);

    @Query(value = "SELECT DISTINCT ON (case_uuid, type) a.* FROM audit_event a WHERE a.audit_timestamp BETWEEN ?1 AND ?2 AND a.type in ?3 AND a.case_type = ?4 AND a.deleted = false ORDER BY a.case_uuid, a.type, a.audit_timestamp DESC;", nativeQuery = true)
    Stream<AuditEvent> findLastAuditDataByDateRangeAndEvents(LocalDateTime dateFrom, LocalDateTime dateTo, String[] types, String caseType);

    @Query(value = "SELECT a.* FROM audit_event a WHERE a.case_uuid = ?1", nativeQuery = true)
    List<AuditEvent> findAuditDataByCaseUUID(UUID caseUUID);
}
