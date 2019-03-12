package uk.gov.digital.ho.hocs.audit;

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
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
        log.info("Created Audit: UUID: {}, CaseUUID: {}, StageUUID: {}, Correlation ID: {}, Raised by: {}, By user: {}, at timestamp: {}",
                auditData.getUuid(),
                auditData.getCorrelationID(),
                auditData.getCaseUUID(),
                auditData.getStageUUID(),
                auditData.getRaisingService(),
                auditData.getUserID(),
                auditData.getAuditTimestamp());
        return auditData;
    }

    public AuditData getAuditDataByUUID(UUID auditUUID) {
        log.info("Requesting Audit for Audit UUID: {} ", auditUUID);
        return auditRepository.findAuditDataByUuid(auditUUID);
    }

    public List<AuditData> getAuditDataByCaseUUID(UUID caseUUID, String types) {
        log.info("Requesting Audit for Case UUID: {} ", caseUUID);
        String[] filterTypes = types.split(",");
        return auditRepository.findAuditDataByCaseUUIDAndTypesIn(caseUUID, filterTypes);
    }

    public List<AuditData> getAuditDataList(int page, int limit){
        log.info("Requesting all audits from last seven days");
        pageRequest = PageRequest.of(page,limit);
        LocalDateTime lastWeek = LocalDateTime.now().minusWeeks(1);
        return auditRepository.findAuditData(lastWeek, pageRequest);
    }

    public List<AuditData> getAuditDataByDateRange(String fromDate, String toDate, int page, int limit){
        log.info("Requesting all audits for dates: {} to {} ", fromDate, toDate);
        pageRequest = PageRequest.of(page,limit);
        return auditRepository.findAuditDataByDateRange(convertLocalDateToStartOfLocalDateTime(fromDate), convertLocalDateToEndOfLocalDateTime(toDate), pageRequest);
    }

    public List<AuditData> getAuditDataByCorrelationID(String correlationID, int page, int limit){
        LocalDateTime lastWeek = LocalDateTime.now().minusWeeks(1);
        log.info("Requesting audits for Correlation ID: {} from last seven days", correlationID);
        pageRequest = PageRequest.of(page,limit);
        return auditRepository.findAuditDataByCorrelationID(correlationID, lastWeek, pageRequest);
    }

    public List<AuditData> getAuditDataByUserID(String userID, int page, int limit){
        log.info("Requesting audits for User ID: {} from last seven days", userID);
        pageRequest = PageRequest.of(page,limit);
        LocalDateTime lastWeek = LocalDateTime.now().minusWeeks(1);
        return auditRepository.findAuditDataByUserID(userID, lastWeek, pageRequest);
    }

    public List<AuditData> getAuditDataByUserIDByDateRange(String userID, String fromDate, String toDate, int page, int limit){
        log.info("Requesting audits for User IDL {}, from dates: {} to {} ", userID, fromDate, toDate);
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
                log.info("Created audit with invalid json in payload - Correlation ID: {}, Raised by: {}, Namespace: {}, Timestamp: {}, EventType: {}, User: {}\")",
                        correlationID,
                        raisingService,
                        namespace,
                        auditTimestamp,
                        type,
                        userID);
                // Encode invalid json to base 64, otherwise it can be seen as nested invalid json
                byte[] encodedPayload = Base64.getEncoder().encode(auditPayload.getBytes());
                return "{\"invalid_json\":\"" + new String(encodedPayload) + "\"}";
            }
        }
        return auditPayload;
    }
}