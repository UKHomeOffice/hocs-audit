package uk.gov.digital.ho.hocs.audit.entrypoint;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.audit.entrypoint.dto.CreateAuditDto;
import uk.gov.digital.ho.hocs.audit.service.AuditEventService;

@Service
public class AuditListener {

    private final ObjectMapper objectMapper;
    private final AuditEventService auditEventService;

    public AuditListener(ObjectMapper objectMapper,
                         AuditEventService auditEventService) {
        this.objectMapper = objectMapper;
        this.auditEventService = auditEventService;
    }

    @SqsListener(value = "${aws.sqs.audit.url}", deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
    public void onAuditEvent(String message) throws JsonProcessingException {
        CreateAuditDto createAuditEvent = objectMapper.readValue(message, CreateAuditDto.class);

        auditEventService.createAudit(createAuditEvent.getCaseUUID(),
                createAuditEvent.getStageUUID(),
                createAuditEvent.getCorrelationID(),
                createAuditEvent.getRaisingService(),
                createAuditEvent.getAuditPayload(),
                createAuditEvent.getNamespace(),
                createAuditEvent.getAuditTimestamp(),
                createAuditEvent.getType(),
                createAuditEvent.getUserID());
    }

}
