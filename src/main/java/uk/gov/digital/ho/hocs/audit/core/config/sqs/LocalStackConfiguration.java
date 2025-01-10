package uk.gov.digital.ho.hocs.audit.core.config.sqs;


import io.awspring.cloud.autoconfigure.sqs.SqsProperties;
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

import java.net.URI;

@Import(SqsBootstrapConfiguration.class)
@Configuration
@Profile({ "local" })
public class LocalStackConfiguration {

    @Primary
    @Bean
    public SqsAsyncClient awsSqsClient(@Value("${aws.sqs.config.url}") URI awsBaseUrl) {

        AwsCredentials credentials = AwsBasicCredentials.create("test", "test");

        return SqsAsyncClient.builder()
                             .region(Region.EU_WEST_2)
                             .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .endpointOverride(awsBaseUrl)
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
    public SqsProperties.Listener listener() {
        var listener = new SqsProperties.Listener();
        listener.setMaxConcurrentMessages(10);
        return listener;
    }

}
