package uk.gov.digital.ho.hocs.audit.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import uk.gov.digital.ho.hocs.audit.repository.AuditRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "classpath:export/cleandown.sql",
     config = @SqlConfig(transactionMode = ISOLATED),
     executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class AuditEventServiceTest {

    private final String correlationID = "CORRELATION_ID";

    private final String raisingService = "RAISING_SERVICE";

    private final String auditPayload = "{\"Test1\":\"Value1\"}";

    private final String namespace = "NAMESPACE";

    private final LocalDateTime dateTime = LocalDateTime.now();

    private final String auditType = "TYPE";

    private final String userID = "USER";

    @Autowired
    private AuditEventService auditService;

    @Autowired
    private AuditRepository auditRepository;

    @Test
    public void shouldCreateAudit() {
        auditService.createAudit(correlationID, raisingService, auditPayload, namespace, dateTime, auditType, userID);

        Assertions.assertEquals(1, auditRepository.count());
    }

    @Test
    public void shouldNotCreateWithNullCorrelationId() {
        Assertions.assertThrows(DataIntegrityViolationException.class, () -> {
            auditService.createAudit(null, raisingService, auditPayload, namespace, dateTime, auditType, userID);
        });
    }

    @Test
    public void shouldNotCreateWithNullRaisingService() {
        Assertions.assertThrows(DataIntegrityViolationException.class, () -> {
            auditService.createAudit(correlationID, null, auditPayload, namespace, dateTime, auditType, userID);
        });
    }

    @Test
    public void shouldNotCreateWithNullNamespace() {
        Assertions.assertThrows(DataIntegrityViolationException.class, () -> {
            auditService.createAudit(correlationID, raisingService, auditPayload, null, dateTime, auditType, userID);
        });
    }

    @Test
    public void shouldNotCreateWithNullTimestamp() {
        Assertions.assertThrows(DataIntegrityViolationException.class, () -> {
            auditService.createAudit(correlationID, raisingService, auditPayload, namespace, null, auditType, userID);
        });
    }

    @Test
    public void shouldNotCreateWithNullType() {
        Assertions.assertThrows(DataIntegrityViolationException.class, () -> {
            auditService.createAudit(correlationID, raisingService, auditPayload, namespace, dateTime, null, userID);
        });
    }

    @Test
    public void shouldNotCreateWithNullUser() {
        Assertions.assertThrows(DataIntegrityViolationException.class, () -> {
            auditService.createAudit(correlationID, raisingService, auditPayload, namespace, dateTime, auditType, null);
        });
    }

    @Test
    public void shouldCreateAuditWhenAuditPayloadIsInvalid() {
        Assertions.assertThrows(DataIntegrityViolationException.class, () -> {
            auditService.createAudit(correlationID, raisingService, "\"Test\" \"Test\"", namespace, dateTime, auditType,
                userID);
        });
    }

    @Test
    public void shouldCreateAuditWhenAuditPayloadIsEmpty() {
        Assertions.assertThrows(DataIntegrityViolationException.class, () -> {
            auditService.createAudit(correlationID, raisingService, "", namespace, dateTime, auditType, userID);
        });
    }

    @Test
    public void shouldCreateAuditWhenAuditPayloadIsNull() {
        auditService.createAudit(correlationID, raisingService, null, namespace, dateTime, auditType, userID);

        Assertions.assertEquals(1, auditRepository.count());
    }

    @Test
    public void shouldGetAuditForCase() {
        UUID caseUuid = UUID.randomUUID();

        // setup case preparation
        auditService.createAudit(caseUuid, UUID.randomUUID(), correlationID, raisingService, null, namespace, dateTime,
            auditType, userID);

        var audits = auditService.getAuditDataByCaseUUID(caseUuid, new String[] { auditType });

        Assertions.assertEquals(1, audits.size());
    }

    @Test
    public void shouldGetAuditForCaseWithBefore() {
        UUID caseUuid = UUID.randomUUID();

        // setup case preparation
        auditService.createAudit(caseUuid, UUID.randomUUID(), correlationID, raisingService, null, namespace, dateTime,
            auditType, userID);

        var audits = auditService.getAuditDataByCaseUUID(caseUuid, new String[] { auditType },
            LocalDate.now().minusDays(1));

        Assertions.assertEquals(1, audits.size());
    }

    @Test
    public void deleteCaseAuditShouldMarkAsDeleted() {
        UUID caseUuid = UUID.randomUUID();

        // setup case preparation
        auditService.createAudit(caseUuid, UUID.randomUUID(), correlationID, raisingService, null, namespace, dateTime,
            auditType, userID);

        auditService.deleteCaseAudit(caseUuid, true);

        var audits = auditRepository.findAuditDataByCaseUUID(caseUuid);
        Assertions.assertEquals(1, audits.size());
        Assertions.assertTrue(audits.get(0).getDeleted());
    }

}
