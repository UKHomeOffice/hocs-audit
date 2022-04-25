package uk.gov.digital.ho.hocs.audit.entrypoint;

import com.google.gson.Gson;
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.audit.entrypoint.dto.CreateAuditDto;
import uk.gov.digital.ho.hocs.audit.service.AuditEventService;

@Service
public class AuditListener {

    private final Gson gson;
    private final AuditEventService auditEventService;

    public AuditListener(Gson gson,
                         AuditEventService auditEventService) {
        this.gson = gson;
        this.auditEventService = auditEventService;
    }

    @SqsListener(value = "${aws.sqs.audit.url}", deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
    public void onAuditEvent(String message) {
        CreateAuditDto createAuditEvent = gson.fromJson(message, CreateAuditDto.class);

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
