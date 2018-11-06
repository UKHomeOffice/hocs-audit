package uk.gov.digital.ho.hocs.audit;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.digital.ho.hocs.audit.auditdetails.dto.CreateAuditResponse;
import uk.gov.digital.ho.hocs.audit.auditdetails.repository.AuditRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = HocsAuditApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuditResourceIntTest {

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private AuditRepository auditRepository;

    private UUID auditUUID;
    private String correlationID;
    private String userID;

    @Before
    public void setup() {

        clearDatabase();
        HttpHeaders requestHeaders = buildHttpHeaders();
        Map<String, String> body = buildCreateAuditBody();
        HttpEntity<?> auditHttpEntity = new HttpEntity<Object>(body, requestHeaders);

        ResponseEntity<CreateAuditResponse> auditResponse = restTemplate.postForEntity("/audit", auditHttpEntity, CreateAuditResponse.class);
        assertThat(auditResponse.getStatusCode()).isEqualTo(OK);

        assertThat(auditResponse.getBody()).hasFieldOrProperty("uuid");
        auditUUID = auditResponse.getBody().getUuid();
        assertThat(auditUUID).isNotNull();

        assertThat(auditResponse.getBody()).hasFieldOrProperty("correlationID");
        correlationID = auditResponse.getBody().getCorrelationID();
        assertThat(correlationID).isNotNull();

        assertThat(auditResponse.getBody()).hasFieldOrProperty("userID");
        userID = auditResponse.getBody().getUserID();
        assertThat(userID).isNotNull();

    }

    @Test
    public void shouldCreateAudit() {
        HttpHeaders requestHeaders = buildHttpHeaders();
        Map<String, String> body = buildCreateAuditBody();

        HttpEntity<?> httpEntity = new HttpEntity<Object>(body, requestHeaders);

        ResponseEntity<CreateAuditResponse> responseEntity = restTemplate.postForEntity("/audit", httpEntity, CreateAuditResponse.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
        assertThat(responseEntity.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON_UTF8);
        assertThat(responseEntity.getBody()).hasFieldOrProperty("uuid");
        assertThat(responseEntity.getBody()).hasFieldOrProperty("correlationID");
        assertThat(responseEntity.getBody()).hasFieldOrProperty("userID");


    }

    @Test
    public void shouldReturnBadRequestWhenBodyMissingOnCreateAudit() {
        HttpHeaders requestHeaders = buildHttpHeaders();
        Map<String, String> body = new HashMap<>();
        HttpEntity<?> httpEntity = new HttpEntity<Object>(body, requestHeaders);

        ResponseEntity<CreateAuditResponse> responseEntity = restTemplate.postForEntity(
                "/audit",
                httpEntity,
                CreateAuditResponse.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(BAD_REQUEST);
    }

    @After
    public void tearDown() {
        clearDatabase();
    }
    

    private Map<String, String> buildCreateAuditBody() {
        Map<String, String> body = new HashMap<>();

        body.put("correlation_id", "Correlation ID");
        body.put("raising_service", "Raising Service");
        body.put("audit_payload", "{\"name1\":\"value1\",\"name2\":\"value2\"}");
        body.put("namespace", "Namespace");
        body.put("audit_timestamp", LocalDateTime.now().toString());
        body.put("type", "Type");
        body.put("user_id", "User ID");
        return body;
    }

    private HttpHeaders buildHttpHeaders() {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
        requestHeaders.set("X-Auth-Userid", "1");
        requestHeaders.set("X-Auth-Username", "bob");
        requestHeaders.set("x-correlation-id", "12");
        return requestHeaders;
    }

    private void clearDatabase() {
        auditRepository.deleteAll();
    }

}
