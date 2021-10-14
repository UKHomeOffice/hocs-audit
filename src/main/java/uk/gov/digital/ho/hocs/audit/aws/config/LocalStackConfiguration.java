package uk.gov.digital.ho.hocs.audit.aws.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import com.amazonaws.services.sqs.model.PurgeQueueRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.aws.messaging.config.SimpleMessageListenerContainerFactory;
import org.springframework.cloud.aws.messaging.config.annotation.EnableSqs;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@EnableSqs
@Configuration
@Profile({"local"})
public class LocalStackConfiguration {

    @Primary
    @Bean
    public AmazonSQSAsync awsSqsClient(
            @Value("${aws.queue.audit.name}") String queueName,
            @Value("${aws.sqs.config.url}") String awsBaseUrl) {
        var queueClient =  AmazonSQSAsyncClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(
                        new BasicAWSCredentials("test", "test")))
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(awsBaseUrl, "eu-west-2"))
                .build();

        createQueue(queueClient, queueName);

        return queueClient;
    }

    @Primary
    @Bean
    public SimpleMessageListenerContainerFactory simpleMessageListenerContainerFactory(AmazonSQSAsync amazonSqs) {
        SimpleMessageListenerContainerFactory factory = new SimpleMessageListenerContainerFactory();

        factory.setAmazonSqs(amazonSqs);
        factory.setMaxNumberOfMessages(10);

        return factory;
    }

    private void createQueue(final AmazonSQSAsync amazonSqsAsync, final String queueName) {
        var createQueueResult = amazonSqsAsync.createQueue(queueName);
        amazonSqsAsync.purgeQueue(new PurgeQueueRequest(createQueueResult.getQueueUrl()));
    }

}
