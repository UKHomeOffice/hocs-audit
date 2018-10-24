package uk.gov.digital.ho.hocs.audit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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

    public AuditData createAudit(String correlationID, String raisingService, String before, String after, String namespace, String type, String userID){
        log.debug("Creating Audit: {}, ID: {}, Raised by: {}, By user: {}", correlationID, raisingService, userID);
        AuditData auditData = new AuditData(correlationID, raisingService, before, after, namespace, type, userID);
        auditRepository.save(auditData);
        log.info("Creating Audit: {}, ID: {}, Raised by: {}, By user: {}", correlationID, raisingService, userID);
        return auditData;
    }
}