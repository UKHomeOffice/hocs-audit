package uk.gov.digital.ho.hocs.audit.entrypoint.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesRequest;
import software.amazon.awssdk.services.sqs.model.PurgeQueueRequest;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;

import java.util.concurrent.ExecutionException;

@SpringBootTest
@ActiveProfiles({"local", "consumer"})
public class BaseAwsSqsIntegrationTest {

    @Autowired
    protected SqsAsyncClient amazonSQSAsync;

    @Value("${aws.sqs.audit.url}")
    protected String auditQueue;

    @Value("${aws.sqs.audit-dlq.url}")
    protected String auditQueueDlq;

    @BeforeEach
    public void setup() {
        amazonSQSAsync.purgeQueue(PurgeQueueRequest.builder().queueUrl(auditQueue).build());
        amazonSQSAsync.purgeQueue(PurgeQueueRequest.builder().queueUrl(auditQueueDlq).build());
    }

    @AfterEach
    public void teardown() {
        amazonSQSAsync.purgeQueue(PurgeQueueRequest.builder().queueUrl(auditQueue).build());
        amazonSQSAsync.purgeQueue(PurgeQueueRequest.builder().queueUrl(auditQueueDlq).build());
    }

    public int getNumberOfMessagesOnQueue(String queue) throws ExecutionException, InterruptedException {
        return getValueFromQueue(queue, QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES);
    }

    private int getValueFromQueue(
        String queue,
        QueueAttributeName attribute
    ) throws ExecutionException, InterruptedException {
        var queueAttributes = amazonSQSAsync.getQueueAttributes(
            GetQueueAttributesRequest
                .builder()
                .queueUrl(queue)
                .attributeNames(attribute)
                .build()
        );
        var messageCount = queueAttributes.get().attributes().get(attribute);
        return messageCount == null ? 0 : Integer.parseInt(messageCount);
    }

}
