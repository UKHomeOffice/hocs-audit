package uk.gov.digital.ho.hocs.audit.entrypoint;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import uk.gov.digital.ho.hocs.audit.entrypoint.dto.DeleteCaseAuditDto;
import uk.gov.digital.ho.hocs.audit.entrypoint.dto.DeleteCaseAuditResponse;
import uk.gov.digital.ho.hocs.audit.repository.AuditRepository;
import uk.gov.digital.ho.hocs.audit.repository.entity.AuditEvent;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;

@AutoConfigureTestDatabase(replace= AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "classpath:export/cleandown.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class CaseAuditEventResourceTest extends BaseExportResourceTest {

    private AuditEvent auditEvent;

    @Autowired
    private AuditRepository auditRepository;

    @BeforeEach
    public void setup() {
        auditEvent = new AuditEvent(UUID.randomUUID(), UUID.randomUUID(), "TEST", "TEST",
                "{}", "TEST", LocalDateTime.now(), "TEST", "TEST");

        auditRepository.save(auditEvent);
    }

    @Test
    public void shouldDeleteCaseAudit() {
        DeleteCaseAuditDto deleteCaseAuditDto = new DeleteCaseAuditDto("1", true);
        HttpEntity<DeleteCaseAuditDto> httpEntity = new HttpEntity<>(deleteCaseAuditDto, null);

        ResponseEntity<DeleteCaseAuditResponse> result = restTemplate.exchange(getExportUri("/audit/case/%s/delete", auditEvent.getCaseUUID()),
                POST, httpEntity, DeleteCaseAuditResponse.class);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());

        var resultBody = result.getBody();

        Assertions.assertNotNull(resultBody);
        Assertions.assertEquals(auditEvent.getCaseUUID(), resultBody.getCaseUUID());
        Assertions.assertEquals(1, resultBody.getAuditCount());
    }

}
