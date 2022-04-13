package uk.gov.digital.ho.hocs.audit.auditdetails.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.audit.auditdetails.model.AuditEvent;
import javax.persistence.QueryHint;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.hibernate.jpa.QueryHints.HINT_FETCH_SIZE;

@Repository
public interface AuditRepository extends PagingAndSortingRepository<AuditEvent, String>, AuditRepositoryCustom {


    AuditEvent findAuditDataByUuid(UUID uuid);

    @Query(value = "SELECT a.* FROM audit_event a WHERE a.correlation_id = ?1 AND a.audit_timestamp > ?2 AND a.deleted = false", nativeQuery = true)
    List<AuditEvent> findAuditDataByCorrelationID(String correlationID, LocalDateTime dateTime, Pageable pageRequest);

    @Query(value = "SELECT a.* FROM audit_event a WHERE a.user_id = ?1 AND a.audit_timestamp > ?2 AND a.deleted = false", nativeQuery = true)
    List<AuditEvent> findAuditDataByUserID(String userID, LocalDateTime dateTime, Pageable pageRequest);

    @Query(value = "SELECT a.* FROM audit_event a WHERE a.user_id = ?1 AND a.deleted = false AND BETWEEN a.audit_timestamp = ?2 AND a.audit_timestamp = ?3", nativeQuery = true)
    List<AuditEvent> findAuditDataByUserIDAndDateRange(String userID, LocalDateTime dateFrom, LocalDateTime dateTo, Pageable pageRequest);

    @Query(value = "SELECT a.* FROM audit_event a WHERE a.audit_timestamp > ?1 AND a.deleted = false", nativeQuery = true)
    List<AuditEvent> findAuditData(LocalDateTime dateTime, Pageable pageRequest);

    @QueryHints(value = @QueryHint(name = HINT_FETCH_SIZE, value = "50"))
    @Query(value = "SELECT a.* FROM audit_event a WHERE a.audit_timestamp > ?1 AND a.audit_timestamp < ?2 AND a.deleted = false", nativeQuery = true)
    Stream<AuditEvent> findAuditDataByDateRange(LocalDateTime dateFrom, LocalDateTime dateTo, Pageable pageRequest);

    @Query(value = "SELECT a.* FROM audit_event a WHERE a.case_uuid = ?1 AND a.type IN ?2 AND a.deleted = false", nativeQuery = true)
    List<AuditEvent> findAuditDataByCaseUUIDAndTypesIn(UUID caseUUID, String[] types);

    @QueryHints(value = @QueryHint(name = HINT_FETCH_SIZE, value = "50"))
    @Query(value = "SELECT a.* FROM audit_event a WHERE a.audit_timestamp BETWEEN ?1 AND ?2 AND a.type in ?3 AND a.case_type = ?4 AND a.deleted = false ORDER BY a.audit_timestamp ASC", nativeQuery = true)
    Stream<AuditEvent> findAuditDataByDateRangeAndEvents(LocalDateTime dateFrom, LocalDateTime dateTo, String[] types, String caseType);

    @QueryHints(value = @QueryHint(name = HINT_FETCH_SIZE, value = "50"))
    @Query(value = "SELECT DISTINCT ON (case_uuid, type) a.* FROM audit_event a WHERE a.audit_timestamp BETWEEN ?1 AND ?2 AND a.type in ?3 AND a.case_type = ?4 AND a.deleted = false ORDER BY a.case_uuid, a.type, a.audit_timestamp DESC;", nativeQuery = true)
    Stream<AuditEvent> findLastAuditDataByDateRangeAndEvents(LocalDateTime dateFrom, LocalDateTime dateTo, String[] types, String caseType);

    @Query(value = "SELECT a.* FROM audit_event a WHERE a.case_uuid = ?1", nativeQuery = true)
    List<AuditEvent> findAuditDataByCaseUUID(UUID caseUUID);
}
