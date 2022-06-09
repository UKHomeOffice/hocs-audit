package uk.gov.digital.ho.hocs.audit.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Slf4j
@Component
public class RestHelper {

    private final String basicAuth;

    private final RestTemplate restTemplate;

    private final RequestData requestData;

    @Autowired
    public RestHelper(RestTemplate restTemplate, @Value("${hocs.basicauth}") String basicAuth, RequestData requestData) {
        this.restTemplate = restTemplate;
        this.basicAuth = basicAuth;
        this.requestData = requestData;
    }

    private static String getBasicAuth(String basicAuth) {
        return String.format("Basic %s", Base64.getEncoder().encodeToString(basicAuth.getBytes(StandardCharsets.UTF_8)));
    }

    public <R> R get(String rootUri, String endpoint, Class<R> type) {
       log.debug("Making GET request {}{}", rootUri, endpoint);
       return restTemplate
               .exchange(rootUri.concat(endpoint), HttpMethod.GET, getAuthenticatedEntity(), type)
               .getBody();
    }

    public <R> R get(String rootUri, String endpoint, ParameterizedTypeReference<R> type) {
        log.debug("Making GET request {}{}", rootUri, endpoint);
        return restTemplate
                .exchange(rootUri.concat(endpoint), HttpMethod.GET, getAuthenticatedEntity(), type)
                .getBody();
    }

    private HttpEntity<?> getAuthenticatedEntity() {
        return new HttpEntity<>(null, createAuthHeaders());
    }

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(AUTHORIZATION, getBasicAuth(basicAuth));
        headers.add(RequestData.GROUP_HEADER, requestData.getGroups());
        headers.add(RequestData.USER_ID_HEADER, requestData.getUserId());
        headers.add(RequestData.USERNAME_HEADER, requestData.getUsername());
        headers.add(RequestData.CORRELATION_ID_HEADER, requestData.getCorrelationId());
        return headers;
    }

}
