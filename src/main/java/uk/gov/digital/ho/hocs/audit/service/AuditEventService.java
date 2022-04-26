package uk.gov.digital.ho.hocs.audit.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.digital.ho.hocs.audit.entrypoint.dto.GetAuditListResponse;
import uk.gov.digital.ho.hocs.audit.entrypoint.dto.GetAuditResponse;
import uk.gov.digital.ho.hocs.audit.repository.AuditRepository;
import uk.gov.digital.ho.hocs.audit.repository.entity.AuditEvent;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.AUDIT_EVENT_DELETED;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.EVENT;

@Service
@Slf4j
public class AuditEventService {

    private final AuditRepository auditRepository;

    private final EntityManager entityManager;

    @Autowired
    public AuditEventService(AuditRepository auditRepository,
                             EntityManager entityManager) {
        this.auditRepository = auditRepository;
        this.entityManager = entityManager;
    }

    public void createAudit(String correlationID, String raisingService, String auditPayload, String namespace, LocalDateTime auditTimestamp, String type, String userID) {
        createAudit(null, null, correlationID, raisingService, auditPayload, namespace, auditTimestamp, type, userID);
    }

    public void createAudit(UUID caseUUID, UUID stageUUID, String correlationID, String raisingService, String auditPayload, String namespace, LocalDateTime auditTimestamp, String type, String userID) {
        var auditEvent = new AuditEvent(caseUUID, stageUUID, correlationID, raisingService, auditPayload, namespace, auditTimestamp, type, userID);
        auditRepository.save(auditEvent);
        log.debug("Created Audit: UUID: {} at timestamp: {}", auditEvent.getUuid(), auditEvent.getAuditTimestamp());
    }

    @Transactional
    public Integer deleteCaseAudit(UUID caseUUID, Boolean deleted) {
        var audits = auditRepository.updateAuditDataDeleted(caseUUID, deleted);
        log.info("Set Deleted=({}) for {} audit lines for caseUUID: {}", deleted, audits, caseUUID, value(EVENT, AUDIT_EVENT_DELETED));
        return audits;
    }

    @Transactional(readOnly = true)
    public GetAuditListResponse getAuditDataByCaseUUID(UUID caseUUID, String[] filterTypes) {
        log.debug("Requesting Audit for Case UUID: {} ", caseUUID);
        var auditResponses = auditRepository.findAuditDataByCaseUUIDAndTypesIn(caseUUID, filterTypes);
        return buildAuditListResponse(auditResponses);
    }

    @Transactional(readOnly = true)
    public GetAuditListResponse getAuditDataByCaseUUID(UUID caseUUID, String[] filterTypes, LocalDate fromDate) {
        log.debug("Requesting Audit for Case UUID: {} FromDate: {}", caseUUID, fromDate);
        var auditResponses = auditRepository.findAuditDataByCaseUUIDAndTypesInAndFrom(caseUUID, filterTypes, fromDate);
        return buildAuditListResponse(auditResponses);
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
}
