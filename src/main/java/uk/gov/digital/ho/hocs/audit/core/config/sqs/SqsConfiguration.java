package uk.gov.digital.ho.hocs.audit.core.config.sqs;


import io.awspring.cloud.autoconfigure.sqs.SqsProperties.Listener;
import io.awspring.cloud.sqs.config.SqsBootstrapConfiguration;
import io.awspring.cloud.sqs.config.SqsMessageListenerContainerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

@Import(SqsBootstrapConfiguration.class)
@Configuration
@Profile({ "sqs" })
public class SqsConfiguration {

    @Primary
    @Bean
    public SqsAsyncClient awsSqsClient(@Value("${aws.sqs.access.key}") String accessKey,
                                       @Value("${aws.sqs.secret.key}") String secretKey,
                                       @Value("${aws.sqs.region}") Region region) {
        AwsCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

        return SqsAsyncClient.builder()
                             .region(region)
                             .credentialsProvider(StaticCredentialsProvider.create(credentials))
                             .build();
    }

    @Primary
    @Bean
    public SqsMessageListenerContainerFactory<Object> defaultSqsListenerContainerFactory(SqsAsyncClient amazonSqs) {
        return SqsMessageListenerContainerFactory
            .builder()
            .sqsAsyncClient(amazonSqs)
            .build();
    }

    @Primary
    @Bean
    public Listener listener() {
        var listener = new Listener();
        listener.setMaxConcurrentMessages(10);
        return listener;
    }
}
