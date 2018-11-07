package uk.gov.digital.ho.hocs.audit.auditdetails.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.audit.auditdetails.model.AuditData;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Repository
public interface AuditRepository extends CrudRepository<AuditData, String> {


    AuditData findByUuid(UUID uuid);

    @Query(value = "SELECT a.* FROM audit_data a WHERE a.correlation_id = ?1 AND a.audit_timestamp > ?2", nativeQuery = true)
    Set<AuditData> findAuditDataByCorrelationID(String correlationID, LocalDateTime dateTime);

    @Query(value = "SELECT a.* FROM audit_data a WHERE a.correlation_id = ?1 AND a.audit_timestamp < ?2 AND a.audit_timestamp > ?3", nativeQuery = true)
    Set<AuditData> findAuditDataByCorrelationIDAndDateRange(String correlationID, LocalDateTime dateFrom, LocalDateTime dateTo);



    AuditData findAuditDataByUUID(UUID uuid);



    @Query(value = "SELECT a.* FROM audit_data a WHERE a.raising_service = ?1 AND a.audit_timestamp > ?2", nativeQuery = true)
    Set<AuditData> findAuditDataByRaisingService(String raisingService, LocalDateTime dateTime);

    @Query(value = "SELECT a.* FROM audit_data a WHERE a.raising_service = ?1 AND a.audit_timestamp < ?2 AND a.audit_timestamp > ?3", nativeQuery = true)
    Set<AuditData> findAuditDataByRaisingServiceAndDateRange(String raisingService, LocalDateTime dateFrom, LocalDateTime dateTo);



    @Query(value = "SELECT a.* FROM audit_data a WHERE a.namespace = ?1 AND a.audit_timestamp > ?2", nativeQuery = true)
    Set<AuditData> findAuditDataByNamespace(String namespace, LocalDateTime dateTime);

    @Query(value = "SELECT a.* FROM audit_data a WHERE a.namespace = ?1 AND a.audit_timestamp < ?2 AND a.audit_timestamp > ?3", nativeQuery = true)
    Set<AuditData> findAuditDataByNamespaceAndDateRange(String namespace, LocalDateTime dateFrom, LocalDateTime dateTo);



    @Query(value = "SELECT a.* FROM audit_data a WHERE a.audit_timestamp > ?1", nativeQuery = true)
    Set<AuditData> findAuditDataDefaultDate(LocalDateTime dateTime);

    @Query(value = "SELECT a.* FROM audit_data a WHERE a.audit_timestamp < ?1 AND a.audit_timestamp > ?2", nativeQuery = true)
    Set<AuditData> findAuditDataByDateRange(LocalDateTime dateFrom, LocalDateTime dateTo);



    @Query(value = "SELECT a.* FROM audit_data a WHERE a.type = ?1 AND a.audit_timestamp > ?2", nativeQuery = true)
    Set<AuditData> findAuditDataByAuditType(String type, LocalDateTime dateTime);

    @Query(value = "SELECT a.* FROM audit_data a WHERE a.type = ?1 AND a.audit_timestamp < ?2 AND a.audit_timestamp > ?3", nativeQuery = true)
    Set<AuditData> findAuditDataByAuditTypeAndDateRange(String type, LocalDateTime dateFrom, LocalDateTime dateTo);



    @Query(value = "SELECT a.* FROM audit_data a WHERE a.user_id = ?1 AND a.audit_timestamp > ?2", nativeQuery = true)
    Set<AuditData> findAuditDataByUserID(String userID, LocalDateTime dateTime);

    @Query(value = "SELECT a.* FROM audit_data a WHERE a.user_id = ?1 AND a.audit_timestamp < ?2 AND a.audit_timestamp > ?3", nativeQuery = true)
    Set<AuditData> findAuditDataByUserIDAndDateRange(String userID, LocalDateTime dateFrom, LocalDateTime dateTo);
}

