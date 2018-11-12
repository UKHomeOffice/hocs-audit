package uk.gov.digital.ho.hocs.audit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.audit.auditdetails.dto.CreateAuditDto;
import uk.gov.digital.ho.hocs.audit.auditdetails.model.AuditData;
import uk.gov.digital.ho.hocs.audit.auditdetails.repository.AuditRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

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

    public AuditData createAudit(CreateAuditDto createAuditDto) {
        AuditData auditData = AuditData.fromDto(createAuditDto);
        auditRepository.save(auditData);
        log.info("Created Audit: UUID: {}, Correlation ID: {}, Raised by: {}, Payload: {}, By user: {}, at timestamp: {}",
                auditData.getUuid(),
                auditData.getCorrelationID(),
                auditData.getRaisingService(),
                auditData.getAuditPayload(),
                auditData.getUserID(),
                auditData.getAuditTimestamp());
        return auditData;
    }

    public AuditData getAuditDataByUUID(UUID auditUUID) {
        log.info("Requesting Audit for Audit UUID: {} ", auditUUID);
        return auditRepository.findAuditDataByUuid(auditUUID);
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

    public List<AuditData> getAuditDataByCorrelationIDByDateRange(String correlationID, String fromDate, String toDate, int page, int limit){
        log.info("Requesting audits for Correlation ID: {}, from dates: {} to {} ", correlationID, fromDate, toDate);
        pageRequest = PageRequest.of(page,limit);
        return auditRepository.findAuditDataByCorrelationIDAndDateRange(correlationID, convertLocalDateToStartOfLocalDateTime(fromDate), convertLocalDateToEndOfLocalDateTime(toDate), pageRequest);
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


    public LocalDateTime convertLocalDateToStartOfLocalDateTime(String date){
        LocalDate fromDate = LocalDate.parse(date);
        return LocalDateTime.of(fromDate, LocalTime.MIN);
    }

    public LocalDateTime convertLocalDateToEndOfLocalDateTime(String date){
        LocalDate toDate = LocalDate.parse(date);
        return LocalDateTime.of(toDate, LocalTime.MAX);
    }
}