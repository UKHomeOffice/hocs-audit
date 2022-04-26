package uk.gov.digital.ho.hocs.audit.client.casework;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.audit.client.casework.dto.GetCaseReferenceResponse;
import uk.gov.digital.ho.hocs.audit.client.casework.dto.GetCorrespondentOutlineResponse;
import uk.gov.digital.ho.hocs.audit.client.casework.dto.GetCorrespondentOutlinesResponse;
import uk.gov.digital.ho.hocs.audit.client.casework.dto.GetTopicResponse;
import uk.gov.digital.ho.hocs.audit.client.casework.dto.GetTopicsResponse;
import uk.gov.digital.ho.hocs.audit.core.RestHelper;

import java.util.Set;

@Component
public class CaseworkClient {

    private final RestHelper restHelper;
    private final String serviceBaseURL;

    @Autowired
    public CaseworkClient(RestHelper restHelper,
                          @Value("${hocs.case-service}") String caseworkServiceUri) {
        this.restHelper = restHelper;
        this.serviceBaseURL = caseworkServiceUri;
    }

    public Set<GetCorrespondentOutlineResponse> getAllActiveCorrespondents() {
        GetCorrespondentOutlinesResponse response = restHelper.get(serviceBaseURL, "/correspondents", new ParameterizedTypeReference<>() {});
        return response.getCorrespondents();
    }

    public Set<GetTopicResponse> getAllCaseTopics() {
        GetTopicsResponse response = restHelper.get(serviceBaseURL, "/topics", new ParameterizedTypeReference<>() {});
        return response.getTopics();
    }

    @Cacheable (value = "getCaseReference", unless = "#result == null")
    public GetCaseReferenceResponse getCaseReference(String uuid) {
        return restHelper.get(serviceBaseURL, String.format("/case/reference/%s", uuid));
    }
}
