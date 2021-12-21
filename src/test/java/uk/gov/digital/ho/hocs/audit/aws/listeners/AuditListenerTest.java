package uk.gov.digital.ho.hocs.audit.aws.listeners;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.digital.ho.hocs.audit.AuditDataService;
import uk.gov.digital.ho.hocs.audit.auditdetails.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.audit.auditdetails.repository.AuditRepository;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AuditListenerTest {

    @Autowired
    private Gson gson;

    @Mock
    private AuditRepository auditRepository;

    @Mock
    private AuditDataService auditDataService;

    @Test
    public void callsAuditServiceWithValidCreateCaseMessage() {
        UUID correlationId = UUID.randomUUID();
        String message = String.format("{ correlation_id: \"%s\"}", correlationId);

        AuditListener auditListener = new AuditListener(gson, auditDataService);

        auditListener.onAuditEvent(message);

        verify(auditDataService)
                .createAudit(any(), any(), eq(correlationId.toString()),
                        any(), any(), any(),
                        any(), any(), any());

        verifyNoMoreInteractions(auditDataService);
    }

    @Test(expected = NullPointerException.class)
    public void callsAuditServiceWithNullCreateCaseMessage() {
        AuditListener auditListener = new AuditListener(gson, auditDataService);

        auditListener.onAuditEvent(null);
    }

    @Test(expected = EntityCreationException.class)
    public void callsAuditServiceWithInvalidCreateCaseMessage() {
        String incorrectMessage = "{test:1}";
        AuditListener auditListener = new AuditListener(gson, new AuditDataService(auditRepository));

        auditListener.onAuditEvent(incorrectMessage);
    }

}
