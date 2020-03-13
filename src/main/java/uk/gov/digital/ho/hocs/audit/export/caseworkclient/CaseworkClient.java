package uk.gov.digital.ho.hocs.audit.export.caseworkclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.audit.application.LogEvent;
import uk.gov.digital.ho.hocs.audit.export.RestHelper;
import uk.gov.digital.ho.hocs.audit.export.caseworkclient.dto.GetTopicResponse;
import uk.gov.digital.ho.hocs.audit.export.caseworkclient.dto.GetTopicsResponse;

import java.util.Collections;
import java.util.Set;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.audit.application.LogEvent.*;

@Slf4j
@Component
public class CaseworkClient {

    private final RestHelper restHelper;
    private final String serviceBaseURL;

    @Autowired
    public CaseworkClient(RestHelper restHelper,
                      @Value("${hocs.case-service}") String caseworkService) {
        this.restHelper = restHelper;
        this.serviceBaseURL = caseworkService;
    }

    public Set<GetTopicResponse> getAllCaseTopics() {

        try {
            GetTopicsResponse response = restHelper.get(serviceBaseURL, "/topics", GetTopicsResponse.class);

            Set<GetTopicResponse> topics = response.getTopics();
            log.info("Got {} case topics", topics.size(), value(EVENT, CASEWORK_CLIENT_GET_TOPICS_SUCCESS));
            return topics;
        } catch (Exception e) {
            log.error("Error retrieving case topics: reason: {}, event: {}", e.getMessage(), value(LogEvent.EVENT, CASEWORK_CLIENT_GET_TOPICS_FAILURE));
            return Collections.emptySet();
        }
    }
}
