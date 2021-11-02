package uk.gov.digital.ho.hocs.audit.export.caseworkclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.audit.application.LogEvent;
import uk.gov.digital.ho.hocs.audit.export.RestHelper;
import uk.gov.digital.ho.hocs.audit.export.caseworkclient.dto.*;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

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

    public Set<GetCorrespondentOutlineResponse> getAllActiveCorrespondents(){

        try {
            GetCorrespondentOutlinesResponse response = restHelper.get(serviceBaseURL, "/correspondents", GetCorrespondentOutlinesResponse.class);
            Set<GetCorrespondentOutlineResponse> correspondents = response.getCorrespondents();
            log.info("Got {} all active correspondents", correspondents.size(), value(EVENT, CASEWORK_CLIENT_GET_CORRESPONDENTS_SUCCESS));
            return correspondents;
        } catch (Exception e) {
            log.error("Error retrieving all active correspondents: reason: {}, event: {}", e.getMessage(), value(LogEvent.EVENT, CASEWORK_CLIENT_GET_CORRESPONDENTS_FAILURE));
            return Collections.emptySet();
        }
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

    @Cacheable (value = "getCaseReference", unless = "#result == null")
    public GetCaseReferenceResponse getCaseReference(String uuid) {

        try {
            GetCaseReferenceResponse caseReferenceResponse = restHelper.get(serviceBaseURL, String.format("/case/reference/%s", uuid), GetCaseReferenceResponse.class);
            log.info("Got {} case reference for uuid {}", caseReferenceResponse.getReference(), caseReferenceResponse.getUuid(), value(EVENT, CASEWORK_CLIENT_GET_CASE_REFERENCE_SUCCESS));
            return caseReferenceResponse;
        } catch (Exception e) {
            log.error("Error retrieving case reference: reason: {}, event: {}", e.getMessage(), value(LogEvent.EVENT, CASEWORK_CLIENT_GET_CASE_REFERENCE_FAILURE));
            return new GetCaseReferenceResponse(UUID.fromString(uuid), "");
        }
    }

    public GetCorrespondentsResponse getCaseCorrespondents(String caseUuid){
        try {
            GetCorrespondentsResponse caseCorrespondentsResponse = restHelper.get(serviceBaseURL, String.format("/case/%s/correspondent", caseUuid), GetCorrespondentsResponse.class);
            log.info("Got {} correspondents for uuid {}", caseCorrespondentsResponse.getCorrespondents().size(), caseUuid, value(EVENT, CASEWORK_CLIENT_GET_CASE_CORRESPONDENTS_SUCCESS));
            return caseCorrespondentsResponse;
        } catch (Exception e) {
            log.error("Error retrieving correspondents: reason: {}, event: {}", e.getMessage(), value(LogEvent.EVENT, CASEWORK_CLIENT_GET_CASE_CORRESPONDENTS_FAILURE));
            return new GetCorrespondentsResponse(null);
        }

    }
}
