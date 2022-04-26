package uk.gov.digital.ho.hocs.audit.entrypoint.integration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.digital.ho.hocs.audit.service.AuditEventService;

import java.util.UUID;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@SpringBootTest
@RunWith(SpringRunner.class)
public class CreateAuditTest extends BaseAwsSqsIntegrationTest {

    @MockBean
    public AuditEventService auditEventService;

    @Test
    public void consumeMessageFromQueue() {
        UUID correlationId = UUID.randomUUID();
        String message = String.format("{ \"correlation_id\": \"%s\" }", correlationId);

        amazonSQSAsync.sendMessage(auditQueue, message);

        await().until(() -> getNumberOfMessagesOnQueue() == 0);

        verify(auditEventService).createAudit(any(), any(), eq(correlationId.toString()),
                any(), any(), any(),
                any(), any(), any());
    }

    @Test
    public void consumeMessageFromQueue_exceptionMakesMessageNotVisible() {
        UUID correlationId = UUID.randomUUID();
        String message = String.format("{ \"correlation_id\": \"%s\" }", correlationId);

        doThrow(new NullPointerException("TEST")).when(auditEventService).createAudit(any(), any(), eq(correlationId.toString()),
                any(), any(), any(),
                any(), any(), any());

        amazonSQSAsync.sendMessage(auditQueue, message);

        await().until(() -> getNumberOfMessagesOnQueue() == 0);
        await().until(() -> getNumberOfMessagesNotVisibleOnQueue() == 1);

        verify(auditEventService).createAudit(any(), any(), eq(correlationId.toString()),
                any(), any(), any(),
                any(), any(), any());
    }

}
