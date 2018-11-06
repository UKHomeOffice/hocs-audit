package uk.gov.digital.ho.hocs.audit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.audit.auditdetails.dto.CreateAuditDto;
import uk.gov.digital.ho.hocs.audit.auditdetails.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.audit.auditdetails.model.AuditData;
import uk.gov.digital.ho.hocs.audit.auditdetails.repository.AuditRepository;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.LocalDateTime;


@Service
@Slf4j
public class AuditDataService {

    private final AuditRepository auditRepository;

    @Autowired
    public AuditDataService(AuditRepository auditRepository){
        this.auditRepository = auditRepository;
    }

    public AuditData createAudit(CreateAuditDto createAuditDto) {
        validate(createAuditDto);
        AuditData auditData = AuditData.fromDto(createAuditDto);
        auditRepository.save(auditData);
        log.info("Created Audit: UUID: {}, Correlation ID: {}, Raised by: {}, By user: {}, at timestamp: {}",
                auditData.getUuid(),
                auditData.getCorrelationID(),
                auditData.getRaisingService(),
                auditData.getUserID(),
                auditData.getAuditTimestamp());
        return auditData;
    }


    private void validate(CreateAuditDto createAuditDto) {
        validateNotNull(createAuditDto);
        validatePayload(createAuditDto);
    }

    private void validateNotNull(CreateAuditDto createAuditDto) {
        String correlationID = createAuditDto.getCorrelationID();
        String raisingService = createAuditDto.getRaisingService();
        String namespace = createAuditDto.getNamespace();
        LocalDateTime auditTimestamp = createAuditDto.getAuditTimestamp();
        String type = createAuditDto.getType();
        String userID = createAuditDto.getUserID();

        if (correlationID == null || raisingService == null || namespace == null || auditTimestamp == null || type == null || userID == null) {
            throw new EntityCreationException("Cannot create Audit - null input(%s, %s, %s, %s, %s, %s, %s)",
                    correlationID,
                    raisingService,
                    createAuditDto.getAuditPayload(),
                    namespace,
                    auditTimestamp,
                    type,
                    userID);
        }
    }

    private void validatePayload(CreateAuditDto createAuditDto){
        String auditPayload = createAuditDto.getAuditPayload();
        if (auditPayload != null){
            try {
                final ObjectMapper mapper = new ObjectMapper();
                mapper.readTree(auditPayload);
            } catch (IOException e) {
                throw new EntityCreationException("Cannot create Audit - invalid Json (%s, %s, %s, %s, %s, %s, %s)",
                        createAuditDto.getCorrelationID(),
                        createAuditDto.getRaisingService(),
                        createAuditDto.getAuditPayload(),
                        createAuditDto.getNamespace(),
                        createAuditDto.getAuditTimestamp(),
                        createAuditDto.getType(),
                        createAuditDto.getUserID());
            }
        }
    }

}