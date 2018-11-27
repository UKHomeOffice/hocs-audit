package uk.gov.digital.ho.hocs.audit.auditdetails.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.audit.auditdetails.model.AuditData;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditRepository extends PagingAndSortingRepository<AuditData, String> {


    AuditData findAuditDataByUuid(UUID uuid);

    @Query(value = "SELECT a.* FROM audit_data a WHERE a.correlation_id = ?1 AND a.audit_timestamp > ?2", nativeQuery = true)
    List<AuditData> findAuditDataByCorrelationID(String correlationID, LocalDateTime dateTime, Pageable pageRequest);

    @Query(value = "SELECT a.* FROM audit_data a WHERE a.correlation_id = ?1 AND BETWEEN a.audit_timestamp = ?2 AND a.audit_timestamp = ?3", nativeQuery = true)
    List<AuditData> findAuditDataByCorrelationIDAndDateRange(String correlationID, LocalDateTime dateFrom, LocalDateTime dateTo, Pageable pageRequest);

    @Query(value = "SELECT a.* FROM audit_data a WHERE a.user_id = ?1 AND a.audit_timestamp > ?2", nativeQuery = true)
    List<AuditData> findAuditDataByUserID(String userID, LocalDateTime dateTime, Pageable pageRequest);

    @Query(value = "SELECT a.* FROM audit_data a WHERE a.user_id = ?1 AND BETWEEN a.audit_timestamp = ?2 AND a.audit_timestamp = ?3", nativeQuery = true)
    List<AuditData> findAuditDataByUserIDAndDateRange(String userID, LocalDateTime dateFrom, LocalDateTime dateTo, Pageable pageRequest);

    @Query(value = "SELECT a.* FROM audit_data a WHERE a.audit_timestamp > ?1", nativeQuery = true)
    List<AuditData> findAuditData(LocalDateTime dateTime, Pageable pageRequest);

    @Query(value = "SELECT a.* FROM audit_data a WHERE a.audit_timestamp > ?1 AND a.audit_timestamp < ?2", nativeQuery = true)
    List<AuditData> findAuditDataByDateRange(LocalDateTime dateFrom, LocalDateTime dateTo, Pageable pageRequest);
}


