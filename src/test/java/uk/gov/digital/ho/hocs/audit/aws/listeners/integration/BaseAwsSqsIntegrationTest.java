package uk.gov.digital.ho.hocs.audit.aws.listeners.integration;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.PurgeQueueRequest;
import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

@ActiveProfiles("local")
public class BaseAwsSqsIntegrationTest {

    private static final String APPROXIMATE_NUMBER_OF_MESSAGES = "ApproximateNumberOfMessages";
    private static final String APPROXIMATE_NUMBER_OF_MESSAGES_NOT_VISIBLE = "ApproximateNumberOfMessagesNotVisible";

    @Autowired
    public AmazonSQSAsync amazonSQSAsync;

    @Value("${aws.sqs.audit.url}")
    public String auditQueue;

    @Before
    public void setup() {
        amazonSQSAsync.purgeQueue(new PurgeQueueRequest(auditQueue));
    }

    @After
    public void teardown() {
        amazonSQSAsync.purgeQueue(new PurgeQueueRequest(auditQueue));
    }

    public int getNumberOfMessagesOnQueue() {
        return getValueFromQueue(auditQueue, APPROXIMATE_NUMBER_OF_MESSAGES);
    }

    public int getNumberOfMessagesNotVisibleOnQueue() {
        return getValueFromQueue(auditQueue, APPROXIMATE_NUMBER_OF_MESSAGES_NOT_VISIBLE);
    }

    private int getValueFromQueue(String queue, String attribute) {
        var queueAttributes = amazonSQSAsync.getQueueAttributes(queue, List.of(attribute));
        var messageCount = queueAttributes.getAttributes().get(attribute);
        return messageCount == null ? 0 : Integer.parseInt(messageCount);
    }


}