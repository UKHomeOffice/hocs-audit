package uk.gov.digital.ho.hocs.audit.core.config.sqs;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.aws.messaging.config.SimpleMessageListenerContainerFactory;
import org.springframework.cloud.aws.messaging.config.annotation.EnableSqs;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@EnableSqs
@Configuration
@Profile({"sqs"})
public class SqsConfiguration {

    @Primary
    @Bean
    public AmazonSQSAsync awsSqsClient(@Value("${aws.sqs.access.key}") String accessKey,
                               @Value("${aws.sqs.secret.key}") String secretKey,
                               @Value("${aws.sqs.region}") String region) {
        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);

        return AmazonSQSAsyncClientBuilder
                .standard()
                .withRegion(region)
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();
    }

    @Primary
    @Bean
    public SimpleMessageListenerContainerFactory simpleMessageListenerContainerFactory(AmazonSQSAsync amazonSqs) {
        SimpleMessageListenerContainerFactory factory = new SimpleMessageListenerContainerFactory();

        factory.setAmazonSqs(amazonSqs);
        factory.setMaxNumberOfMessages(10);

        return factory;
    }
}
