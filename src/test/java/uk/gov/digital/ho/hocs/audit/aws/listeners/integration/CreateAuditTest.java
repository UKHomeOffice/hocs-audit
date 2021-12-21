package uk.gov.digital.ho.hocs.audit.aws.listeners.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.digital.ho.hocs.audit.AuditDataService;

import java.util.UUID;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class CreateAuditTest extends BaseAwsSqsIntegrationTest {

    @MockBean
    public AuditDataService auditDataService;

    @Test
    public void consumeMessageFromQueue() {
        UUID correlationId = UUID.randomUUID();
        String message = String.format("{ correlation_id: \"%s\" }", correlationId);

        amazonSQSAsync.sendMessage(auditQueue, message);

        await().until(() -> getNumberOfMessagesOnQueue() == 0);

        verify(auditDataService).createAudit(any(), any(), eq(correlationId.toString()),
                any(), any(), any(),
                any(), any(), any());
    }

    @Test
    public void consumeMessageFromQueue_exceptionMakesMessageNotVisible() {
        UUID correlationId = UUID.randomUUID();
        String message = String.format("{ correlation_id: \"%s\" }", correlationId);

        when(auditDataService.createAudit(any(), any(), eq(correlationId.toString()),
                any(), any(), any(),
                any(), any(), any())).thenThrow(new NullPointerException("TEST"));

        amazonSQSAsync.sendMessage(auditQueue, message);

        await().until(() -> getNumberOfMessagesOnQueue() == 0);
        await().until(() -> getNumberOfMessagesNotVisibleOnQueue() == 1);

        verify(auditDataService).createAudit(any(), any(), eq(correlationId.toString()),
                any(), any(), any(),
                any(), any(), any());
    }

}
