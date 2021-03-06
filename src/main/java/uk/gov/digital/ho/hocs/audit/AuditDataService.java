package uk.gov.digital.ho.hocs.audit;

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import uk.gov.digital.ho.hocs.audit.auditdetails.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.audit.auditdetails.model.AuditData;
import uk.gov.digital.ho.hocs.audit.auditdetails.repository.AuditRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.audit.application.LogEvent.*;

@Service
@Slf4j
public class AuditDataService {

    private final AuditRepository auditRepository;

    private Pageable pageRequest;

    @Autowired
    public AuditDataService(AuditRepository auditRepository){
        this.auditRepository = auditRepository;
    }


    public AuditData createAudit(String correlationID, String raisingService, String auditPayload, String namespace, LocalDateTime auditTimestamp, String type, String userID) {
         return createAudit(null, null, correlationID, raisingService, auditPayload, namespace, auditTimestamp, type, userID);
    }

    public AuditData createAudit(UUID caseUUID, UUID stageUUID, String correlationID, String raisingService, String auditPayload, String namespace, LocalDateTime auditTimestamp, String type, String userID) {
        String validAuditPayload = validatePayload(correlationID, raisingService, auditPayload, namespace, auditTimestamp, type, userID);
        AuditData auditData = new AuditData(caseUUID, stageUUID, correlationID, raisingService, validAuditPayload, namespace, auditTimestamp, type, userID);
        validateNotNull(auditData);
        auditRepository.save(auditData);
        log.info("Created Audit: UUID: {}, CaseUUID: {}, StageUUID: {}, Correlation ID: {}, Raised by: {}, By user: {}, at timestamp: {}, event {}",
                auditData.getUuid(),
                auditData.getCaseUUID(),
                auditData.getStageUUID(),
                auditData.getCorrelationID(),
                auditData.getRaisingService(),
                auditData.getUserID(),
                auditData.getAuditTimestamp(), value(EVENT, AUDIT_EVENT_CREATED));
        return auditData;
    }

    public Integer deleteCaseAudit(UUID caseUUID, Boolean deleted){
        log.debug("Case {} setting deleted to {}", caseUUID, deleted);
        List<AuditData> audits = auditRepository.findAuditDataByCaseUUID(caseUUID);
        for (AuditData audit : audits) {
            audit.setDeleted(deleted);
            auditRepository.save(audit);
        }
        log.info("Case {} set {} audits deleted to {}", caseUUID, audits.size(), deleted);
        return audits.size();
    }

    @Transactional(readOnly = true)
    public AuditData getAuditDataByUUID(UUID auditUUID) {
        log.debug("Requesting Audit for Audit UUID: {} ", auditUUID);
        return auditRepository.findAuditDataByUuid(auditUUID);
    }

    @Transactional(readOnly = true)
    public List<AuditData> getAuditDataByCaseUUID(UUID caseUUID, String types) {
        log.debug("Requesting Audit for Case UUID: {} ", caseUUID);
        String[] filterTypes = types.split(",");
        return auditRepository.findAuditDataByCaseUUIDAndTypesIn(caseUUID, filterTypes);
    }

    @Transactional(readOnly = true)
    public List<AuditData> getAuditDataList(int page, int limit){
        log.info("Requesting all audits from last seven days");
        pageRequest = PageRequest.of(page,limit);
        LocalDateTime lastWeek = LocalDateTime.now().minusWeeks(1);
        return auditRepository.findAuditData(lastWeek, pageRequest);
    }

    @Transactional(readOnly = true)
    public List<AuditData> getAuditDataByDateRange(String fromDate, String toDate, int page, int limit){
        log.debug("Requesting all audits for dates: {} to {} ", fromDate, toDate);
        pageRequest = PageRequest.of(page,limit);
        return auditRepository.findAuditDataByDateRange(convertLocalDateToStartOfLocalDateTime(fromDate), convertLocalDateToEndOfLocalDateTime(toDate), pageRequest).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AuditData> getAuditDataByCorrelationID(String correlationID, int page, int limit){
        LocalDateTime lastWeek = LocalDateTime.now().minusWeeks(1);
        log.debug("Requesting audits for Correlation ID: {} from last seven days", correlationID);
        pageRequest = PageRequest.of(page,limit);
        return auditRepository.findAuditDataByCorrelationID(correlationID, lastWeek, pageRequest);
    }

    @Transactional(readOnly = true)
    public List<AuditData> getAuditDataByUserID(String userID, int page, int limit){
        log.debug("Requesting audits for User ID: {} from last seven days", userID);
        pageRequest = PageRequest.of(page,limit);
        LocalDateTime lastWeek = LocalDateTime.now().minusWeeks(1);
        return auditRepository.findAuditDataByUserID(userID, lastWeek, pageRequest);
    }

    @Transactional(readOnly = true)
    public List<AuditData> getAuditDataByUserIDByDateRange(String userID, String fromDate, String toDate, int page, int limit){
        log.debug("Requesting audits for User IDL {}, from dates: {} to {} ", userID, fromDate, toDate);
        pageRequest = PageRequest.of(page,limit);
        return auditRepository.findAuditDataByUserIDAndDateRange(userID, convertLocalDateToStartOfLocalDateTime(fromDate), convertLocalDateToEndOfLocalDateTime(toDate), pageRequest);
    }

    private LocalDateTime convertLocalDateToStartOfLocalDateTime(String date){
        LocalDate fromDate = LocalDate.parse(date);
        return LocalDateTime.of(fromDate, LocalTime.MIN);
    }

    private LocalDateTime convertLocalDateToEndOfLocalDateTime(String date){
        LocalDate toDate = LocalDate.parse(date);
        return LocalDateTime.of(toDate, LocalTime.MAX);
    }

    private static void validateNotNull(AuditData auditData) {
        String correlationID = auditData.getCorrelationID();
        String raisingService = auditData.getRaisingService();
        String namespace = auditData.getNamespace();
        LocalDateTime auditTimestamp = auditData.getAuditTimestamp();
        String type = auditData.getType();
        String userID = auditData.getUserID();

        if (correlationID == null || raisingService == null || namespace == null || auditTimestamp == null || type == null || userID == null) {
            throw new EntityCreationException("Cannot create Audit - null input(%s, %s, %s, %s, %s, %s, %s)",
                    correlationID,
                    raisingService,
                    auditData.getAuditPayload(),
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
                        userID, value(EVENT, INVALID_AUDIT_PALOAD_STORED));
                // Encode invalid json to base 64, otherwise it can be seen as nested invalid json
                byte[] encodedPayload = Base64.getEncoder().encode(auditPayload.getBytes());
                return "{\"invalid_json\":\"" + new String(encodedPayload) + "\"}";
            }
        }
        return auditPayload;
    }
}