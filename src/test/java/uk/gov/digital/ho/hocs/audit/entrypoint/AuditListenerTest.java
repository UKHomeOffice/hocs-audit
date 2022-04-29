package uk.gov.digital.ho.hocs.audit.entrypoint;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.digital.ho.hocs.audit.core.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.audit.core.utils.JsonValidator;
import uk.gov.digital.ho.hocs.audit.repository.AuditRepository;
import uk.gov.digital.ho.hocs.audit.service.AuditEventService;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AuditListenerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Mock
    private AuditRepository auditRepository;

    @Mock
    private AuditEventService auditEventService;

    @Mock
    private JsonValidator jsonValidator;

    @Test
    public void callsAuditServiceWithValidCreateCaseMessage() throws JsonProcessingException {
        UUID correlationId = UUID.randomUUID();
        String message = String.format("{ \"correlation_id\": \"%s\"}", correlationId);

        AuditListener auditListener = new AuditListener(objectMapper, auditEventService);

        auditListener.onAuditEvent(message);

        verify(auditEventService)
                .createAudit(any(), any(), eq(correlationId.toString()),
                        any(), any(), any(),
                        any(), any(), any());

        verifyNoMoreInteractions(auditEventService);
    }

    @Test(expected = IllegalArgumentException.class)
    public void callsAuditServiceWithNullCreateCaseMessage() throws JsonProcessingException {
        AuditListener auditListener = new AuditListener(objectMapper, auditEventService);

        auditListener.onAuditEvent(null);
    }

    @Test(expected = EntityCreationException.class)
    public void callsAuditServiceWithInvalidCreateCaseMessage() throws JsonProcessingException {
        String incorrectMessage = "{\"test\":1}";
        AuditListener auditListener = new AuditListener(objectMapper, new AuditEventService(auditRepository, jsonValidator));

        auditListener.onAuditEvent(incorrectMessage);
    }

}