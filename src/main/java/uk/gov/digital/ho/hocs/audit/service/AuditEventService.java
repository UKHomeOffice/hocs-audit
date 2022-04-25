package uk.gov.digital.ho.hocs.audit.service;

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import uk.gov.digital.ho.hocs.audit.core.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.audit.repository.AuditRepository;
import uk.gov.digital.ho.hocs.audit.repository.entity.AuditEvent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.AUDIT_EVENT_CREATED;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.EVENT;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.INVALID_AUDIT_PAYLOAD_STORED;

@Service
@Slf4j
public class AuditEventService {

    private final AuditRepository auditRepository;

    @Autowired
    public AuditEventService(AuditRepository auditRepository){
        this.auditRepository = auditRepository;
    }


    public AuditEvent createAudit(String correlationID, String raisingService, String auditPayload, String namespace, LocalDateTime auditTimestamp, String type, String userID) {
         return createAudit(null, null, correlationID, raisingService, auditPayload, namespace, auditTimestamp, type, userID);
    }

    public AuditEvent createAudit(UUID caseUUID, UUID stageUUID, String correlationID, String raisingService, String auditPayload, String namespace, LocalDateTime auditTimestamp, String type, String userID) {
        String validAuditPayload = validatePayload(correlationID, raisingService, auditPayload, namespace, auditTimestamp, type, userID);
        AuditEvent auditEvent = new AuditEvent(caseUUID, stageUUID, correlationID, raisingService, validAuditPayload, namespace, auditTimestamp, type, userID);
        validateNotNull(auditEvent);
        auditRepository.save(auditEvent);
        log.debug("Created Audit: UUID: {}, CaseUUID: {}, StageUUID: {}, Correlation ID: {}, Raised by: {}, By user: {}, at timestamp: {}, event {}",
                auditEvent.getUuid(),
                auditEvent.getCaseUUID(),
                auditEvent.getStageUUID(),
                auditEvent.getCorrelationID(),
                auditEvent.getRaisingService(),
                auditEvent.getUserID(),
                auditEvent.getAuditTimestamp(), value(EVENT, AUDIT_EVENT_CREATED));
        return auditEvent;
    }

    public Integer deleteCaseAudit(UUID caseUUID, Boolean deleted){
        log.debug("Case {} setting deleted to {}", caseUUID, deleted);
        List<AuditEvent> audits = auditRepository.findAuditDataByCaseUUID(caseUUID);
        for (AuditEvent audit : audits) {
            audit.setDeleted(deleted);
            auditRepository.save(audit);
        }
        log.info("Case {} set {} audits deleted to {}", caseUUID, audits.size(), deleted);
        return audits.size();
    }

    @Transactional(readOnly = true)
    public List<AuditEvent> getAuditDataByCaseUUID(UUID caseUUID, String types) {
        log.debug("Requesting Audit for Case UUID: {} ", caseUUID);
        String[] filterTypes = types.split(",");
        return auditRepository.findAuditDataByCaseUUIDAndTypesIn(caseUUID, filterTypes);
    }

    public List<AuditEvent> getAuditDataByCaseUUID(UUID caseUUID, String types, LocalDate from) {
        log.debug("Requesting Audit for Case UUID: {} ", caseUUID);
        String[] filterTypes = types.split(",");
        return auditRepository.findAuditDataByCaseUUIDAndTypesInAndFrom(caseUUID, filterTypes, from);
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

    private static String validatePayload(String correlationID, String raisingService, String auditPayload, String namespace, LocalDateTime auditTimestamp, String type, String userID) {
        if (StringUtils.isEmpty(auditPayload)) {
            return "{}";
        }
        else {
            try {
                com.google.gson.JsonParser parser = new JsonParser();
                parser.parse(auditPayload);
            } catch (JsonSyntaxException e) {
                log.warn("Created audit with invalid json in payload - Correlation ID: {}, Raised by: {}, Namespace: {}, Timestamp: {}, EventType: {}, User: {}",
                        correlationID,
                        raisingService,
                        namespace,
                        auditTimestamp,
                        type,
                        userID, value(EVENT, INVALID_AUDIT_PAYLOAD_STORED));
                // Encode invalid json to base 64, otherwise it can be seen as nested invalid json
                byte[] encodedPayload = Base64.getEncoder().encode(auditPayload.getBytes());
                return "{\"invalid_json\":\"" + new String(encodedPayload) + "\"}";
            }
        }
        return auditPayload;
    }
}
