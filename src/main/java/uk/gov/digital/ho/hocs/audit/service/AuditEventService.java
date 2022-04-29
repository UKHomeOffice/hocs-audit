package uk.gov.digital.ho.hocs.audit.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.audit.core.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.audit.core.utils.JsonValidator;
import uk.gov.digital.ho.hocs.audit.entrypoint.dto.GetAuditListResponse;
import uk.gov.digital.ho.hocs.audit.entrypoint.dto.GetAuditResponse;
import uk.gov.digital.ho.hocs.audit.repository.AuditRepository;
import uk.gov.digital.ho.hocs.audit.repository.entity.AuditEvent;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.AUDIT_EVENT_CREATED;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.EVENT;

@Service
@Slf4j
public class AuditEventService {

    private final AuditRepository auditRepository;

    private final JsonValidator jsonValidator;

    private final EntityManager entityManager;

    @Autowired
    public AuditEventService(AuditRepository auditRepository,
                             JsonValidator jsonValidator,
                             EntityManager entityManager) {
        this.auditRepository = auditRepository;
        this.jsonValidator = jsonValidator;
        this.entityManager = entityManager;
    }

    public AuditEvent createAudit(String correlationID, String raisingService, String auditPayload, String namespace, LocalDateTime auditTimestamp, String type, String userID) {
        return createAudit(null, null, correlationID, raisingService, auditPayload, namespace, auditTimestamp, type, userID);
    }

    public AuditEvent createAudit(UUID caseUUID, UUID stageUUID, String correlationID, String raisingService, String auditPayload, String namespace, LocalDateTime auditTimestamp, String type, String userID) {
        String validAuditPayload = jsonValidator.validateAuditPayload(correlationID, raisingService, auditPayload, namespace, auditTimestamp, type, userID);
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

    public Integer deleteCaseAudit(UUID caseUUID, Boolean deleted) {
        log.debug("Case {} setting deleted to {}", caseUUID, deleted);
        List<AuditEvent> audits = auditRepository.findAuditDataByCaseUUID(caseUUID);
        for (AuditEvent audit : audits) {
            audit.setDeleted(deleted);
            auditRepository.save(audit);
        }
        log.info("Case {} set {} audits deleted to {}", caseUUID, audits.size(), deleted);
        return audits.size();
    }

    public List<AuditEvent> getAuditDataByCaseUUID(UUID caseUUID, String[] filterTypes) {
        log.debug("Requesting Audit for Case UUID: {} ", caseUUID);
        return auditRepository.findAuditDataByCaseUUIDAndTypesIn(caseUUID, filterTypes);
    }

    public List<AuditEvent> getAuditDataByCaseUUID(UUID caseUUID, String[] filterTypes, LocalDate from) {
        log.debug("Requesting Audit for Case UUID: {} ", caseUUID);
        return auditRepository.findAuditDataByCaseUUIDAndTypesInAndFrom(caseUUID, filterTypes, from);
    }

    private GetAuditListResponse buildAuditListResponse(Stream<AuditEvent> auditEvents) {
        var auditResponses = auditEvents.map(it -> {
                    var response = GetAuditResponse.from(it);
                    entityManager.detach(it);
                    return response;
                })
                .collect(Collectors.toList());
        return new GetAuditListResponse(auditResponses);
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
