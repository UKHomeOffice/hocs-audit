package uk.gov.digital.ho.hocs.audit.entrypoint.integration;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.PurgeQueueRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

@SpringBootTest
@ActiveProfiles("local")
public class BaseAwsSqsIntegrationTest {

    private static final String APPROXIMATE_NUMBER_OF_MESSAGES = "ApproximateNumberOfMessages";

    @Autowired
    protected AmazonSQSAsync amazonSQSAsync;

    @Value("${aws.sqs.audit.url}")
    protected String auditQueue;

    @Value("${aws.sqs.audit-dlq.url}")
    protected String auditQueueDlq;

    @BeforeEach
    public void setup() {
        amazonSQSAsync.purgeQueue(new PurgeQueueRequest(auditQueue));
        amazonSQSAsync.purgeQueue(new PurgeQueueRequest(auditQueueDlq));
    }

    @AfterEach
    public void teardown() {
        amazonSQSAsync.purgeQueue(new PurgeQueueRequest(auditQueue));
        amazonSQSAsync.purgeQueue(new PurgeQueueRequest(auditQueueDlq));
    }

    public int getNumberOfMessagesOnQueue(String queue) {
        return getValueFromQueue(queue, APPROXIMATE_NUMBER_OF_MESSAGES);
    }

    private int getValueFromQueue(String queue, String attribute) {
        var queueAttributes = amazonSQSAsync.getQueueAttributes(queue, List.of(attribute));
        var messageCount = queueAttributes.getAttributes().get(attribute);
        return messageCount == null ? 0 : Integer.parseInt(messageCount);
    }

}
