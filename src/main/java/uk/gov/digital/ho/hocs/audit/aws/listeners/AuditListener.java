package uk.gov.digital.ho.hocs.audit.aws.listeners;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.audit.AuditDataService;
import uk.gov.digital.ho.hocs.audit.auditdetails.dto.CreateAuditDto;

@Service
@Slf4j
public class AuditListener {

    private final Gson gson;
    private final AuditDataService auditDataService;

    public AuditListener(Gson gson,
                         AuditDataService auditDataService) {
        this.gson = gson;
        this.auditDataService = auditDataService;
    }

    @SqsListener(value = "${audit.queue}", deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
    public void onAuditEvent(String message) {
        CreateAuditDto createAuditEvent = gson.fromJson(message, CreateAuditDto.class);

        auditDataService.createAudit(createAuditEvent.getCaseUUID(),
                createAuditEvent.getStageUUID(),
                createAuditEvent.getCorrelationID(),
                createAuditEvent.getRaisingService(),
                createAuditEvent.getAuditPayload(),
                createAuditEvent.getNamespace(),
                createAuditEvent.getAuditTimestamp(),
                createAuditEvent.getType(),
                createAuditEvent.getUserID());

        log.info(message);
    }

}
