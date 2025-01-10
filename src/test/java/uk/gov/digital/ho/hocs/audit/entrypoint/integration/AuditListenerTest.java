package uk.gov.digital.ho.hocs.audit.entrypoint.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import uk.gov.digital.ho.hocs.audit.entrypoint.dto.CreateAuditDto;
import uk.gov.digital.ho.hocs.audit.repository.AuditRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.awaitility.Awaitility.await;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "classpath:export/cleandown.sql", config = @SqlConfig(transactionMode = ISOLATED))
public class AuditListenerTest extends BaseAwsSqsIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuditRepository auditRepository;

    @Test
    public void consumeMessageFromQueue() throws JsonProcessingException {
        CreateAuditDto createAuditDto = new CreateAuditDto(UUID.randomUUID().toString(), "SERVICE", "{}", "NAMESPACE",
            LocalDateTime.now(), "TYPE", "USER");

        amazonSQSAsync.sendMessage(
            SendMessageRequest
                .builder()
                .queueUrl(auditQueue)
                .messageBody(objectMapper.writeValueAsString(createAuditDto))
                .build()
        );

        await().until(() -> getNumberOfMessagesOnQueue(auditQueue) == 0);
        await().until(() -> auditRepository.count() == 1);
    }

    @Test
    public void consumeMessageFromQueue_exceptionMakesMessageNotVisible() throws JsonProcessingException {
        CreateAuditDto createAuditDto = new CreateAuditDto(null, null, null, null, null, null, null);

        amazonSQSAsync.sendMessage(
            SendMessageRequest
                .builder()
                .queueUrl(auditQueue)
                .messageBody(objectMapper.writeValueAsString(createAuditDto))
                .build()
        );

        await().until(() -> getNumberOfMessagesOnQueue(auditQueue) == 0);
        await().timeout(Duration.ofSeconds(20)).pollDelay(Duration.ofSeconds(10)).until(
            () -> getNumberOfMessagesOnQueue(auditQueueDlq) == 1);
    }

}
