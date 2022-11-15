package uk.gov.digital.ho.hocs.audit.entrypoint;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.audit.core.RequestData;
import uk.gov.digital.ho.hocs.audit.entrypoint.dto.CreateAuditDto;
import uk.gov.digital.ho.hocs.audit.service.AuditEventService;

import java.util.Map;

@Service
@Profile("consumer")
public class AuditListener {

    private final ObjectMapper objectMapper;

    private final AuditEventService auditEventService;

    private final RequestData requestData;

    public AuditListener(ObjectMapper objectMapper, AuditEventService auditEventService, RequestData requestData) {
        this.objectMapper = objectMapper;
        this.auditEventService = auditEventService;
        this.requestData = requestData;
    }

    @SqsListener(value = "${aws.sqs.audit.url}", deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
    public void onAuditEvent(String message, @Headers Map<String, String> headers) throws JsonProcessingException {
        try {
            requestData.parseMessageHeaders(headers);
            CreateAuditDto createAuditEvent = objectMapper.readValue(message, CreateAuditDto.class);
            auditEventService.createAudit(createAuditEvent.getCaseUUID(), createAuditEvent.getStageUUID(),
                createAuditEvent.getCorrelationID(), createAuditEvent.getRaisingService(),
                createAuditEvent.getAuditPayload(), createAuditEvent.getNamespace(),
                createAuditEvent.getAuditTimestamp(), createAuditEvent.getType(), createAuditEvent.getUserID());
        } finally {
            requestData.clear();
        }
    }

}
