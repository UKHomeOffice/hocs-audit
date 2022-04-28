package uk.gov.digital.ho.hocs.audit.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.digital.ho.hocs.audit.core.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.audit.core.utils.JsonValidator;
import uk.gov.digital.ho.hocs.audit.repository.AuditRepository;
import uk.gov.digital.ho.hocs.audit.repository.entity.AuditEvent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.AUDIT_EVENT_DELETED;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.EVENT;

@Service
@Slf4j
public class AuditEventService {

    private final AuditRepository auditRepository;

    private final JsonValidator jsonValidator;

    @Autowired
    public AuditEventService(AuditRepository auditRepository,
                             JsonValidator jsonValidator) {
        this.auditRepository = auditRepository;
        this.jsonValidator = jsonValidator;
    }

    public AuditEvent createAudit(String correlationID, String raisingService, String auditPayload, String namespace, LocalDateTime auditTimestamp, String type, String userID) {
        return createAudit(null, null, correlationID, raisingService, auditPayload, namespace, auditTimestamp, type, userID);
    }

    public AuditEvent createAudit(UUID caseUUID, UUID stageUUID, String correlationID, String raisingService, String auditPayload, String namespace, LocalDateTime auditTimestamp, String type, String userID) {
        var auditEvent = new AuditEvent(caseUUID, stageUUID, correlationID, raisingService, auditPayload, namespace, auditTimestamp, type, userID);        validateNotNull(auditEvent);
        auditRepository.save(auditEvent);
        log.debug("Created Audit: UUID: {} at timestamp: {}", auditEvent.getUuid(), auditEvent.getAuditTimestamp());
        return auditEvent;
    }

    public Integer deleteCaseAudit(UUID caseUUID, Boolean deleted){
        List<AuditEvent> audits = auditRepository.findAuditDataByCaseUUID(caseUUID);
        for (AuditEvent audit : audits) {
            audit.setDeleted(deleted);
            auditRepository.save(audit);
        }
        log.info("Set Deleted=({}) for {} audit lines for caseUUID: {}", deleted, audits.size(), caseUUID, value(EVENT, AUDIT_EVENT_DELETED));
        return audits.size();
    }

    @Transactional(readOnly = true)
    public List<AuditEvent> getAuditDataByCaseUUID(UUID caseUUID, String types) {
        log.debug("Requesting Audit for Case UUID: {} ", caseUUID);
        String[] filterTypes = types.split(",");
        return auditRepository.findAuditDataByCaseUUIDAndTypesIn(caseUUID, filterTypes);
    }

    public List<AuditEvent> getAuditDataByCaseUUID(UUID caseUUID, String types, LocalDate fromDate) {
        log.debug("Requesting Audit for Case UUID: {} FromDate: {}", caseUUID, fromDate);
        String[] filterTypes = types.split(",");
        return auditRepository.findAuditDataByCaseUUIDAndTypesInAndFrom(caseUUID, filterTypes, fromDate);
    }

    private static void validateNotNull(AuditEvent auditEvent) {
        String correlationID = auditEvent.getCorrelationID();
        String raisingService = auditEvent.getRaisingService();
        String namespace = auditEvent.getNamespace();
        LocalDateTime auditTimestamp = auditEvent.getAuditTimestamp();
        String type = auditEvent.getType();
        String userID = auditEvent.getUserID();

        if (correlationID == null || raisingService == null || namespace == null || auditTimestamp == null || type == null || userID == null) {
            throw new EntityCreationException("Cannot create Audit - null input(%s, %s, %s, %s, %s, %s, %s)",
                    correlationID,
                    raisingService,
                    auditEvent.getAuditPayload(),
                    namespace,
                    auditTimestamp,
                    type,
                    userID);
        }
    }
}
