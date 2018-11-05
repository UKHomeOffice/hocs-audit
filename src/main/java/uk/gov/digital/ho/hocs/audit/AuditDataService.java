package uk.gov.digital.ho.hocs.audit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.audit.auditdetails.dto.CreateAuditDto;
import uk.gov.digital.ho.hocs.audit.auditdetails.model.AuditData;
import uk.gov.digital.ho.hocs.audit.auditdetails.repository.AuditRepository;

@Service
@Slf4j
public class AuditDataService {

    private final AuditRepository auditRepository;

    @Autowired
    public AuditDataService(AuditRepository auditRepository){
        this.auditRepository = auditRepository;
    }

    public AuditData createAudit(CreateAuditDto createAuditDto){
        AuditData auditData = AuditData.fromDto(createAuditDto);
        auditRepository.save(auditData);
        log.info("Created Audit: UUID: {}, Correlation ID: {}, Raised by: {}, By user: {}, at timestamp: {}", auditData.getUuid(), auditData.getCorrelationID(), auditData.getRaisingService(), auditData.getUserID(), auditData.getAuditTimestamp());
        return auditData;
    }
}