package uk.gov.digital.ho.hocs.audit.repository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.digital.ho.hocs.audit.repository.entity.AuditEvent;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;

@RunWith(SpringRunner.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace= AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "classpath:export/cleandown.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@ActiveProfiles("local")
public class AuditRepositoryTest {
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AuditRepository repository;

    @Before
    public void setup() {
        Set<AuditEvent> data = getCaseDataAuditData();
        for(AuditEvent audit : data) {
            entityManager.persistAndFlush(audit);
        }
    }

    /*
    @Test()
    public void shouldReturnOnlyLatestRowForEachEventAndCase() {

        UUID caseUUID = UUID.fromString("3e5cf44f-e86a-4b21-891a-018e2343cda1");
        Stream<AuditEvent> auditStream = repository.findLastAuditDataByDateRangeAndEvents(LocalDateTime.of(2019,1,1,0,0), LocalDateTime.of(2019,12,31,23,59), ExportService.CASE_DATA_EVENTS,"a1");
        //filter out records not part of the test data
        List<AuditEvent> auditData = auditStream.filter(e-> e.getCaseUUID().equals(caseUUID)).collect(Collectors.toList());
        assertThat(auditData).hasSize(3);
        assertThat(auditData).anyMatch(e-> e.getAuditTimestamp().equals(LocalDateTime.parse("2019-04-24 12:58:04",dateFormatter)));
        assertThat(auditData).anyMatch(e-> e.getAuditTimestamp().equals(LocalDateTime.parse("2019-04-23 09:18:26", dateFormatter)));
        assertThat(auditData).anyMatch(e-> e.getAuditTimestamp().equals(LocalDateTime.parse("2019-04-23 15:20:16", dateFormatter)));
    }
    */

    @Test
    public void shouldFindAuditDataByCaseUUID(){

        UUID caseUUID = UUID.fromString("3e5cf44f-e86a-4b21-891a-018e2343cda1");

        List<AuditEvent> auditData = repository.findAuditDataByCaseUUID(caseUUID);

        assertThat(auditData).hasSize(5);
    }

    private LinkedHashSet<AuditEvent> getCaseDataAuditData() {
        AuditEvent deleted = new AuditEvent(UUID.fromString("3e5cf44f-e86a-4b21-891a-018e2343cda1"),UUID.randomUUID(),UUID.randomUUID().toString(),"a-service", "{\"data\": {\"valid\": \"true\", \"DateReceived\": \"2019-04-23\", \"CopyNumberTen\": \"FALSE\", \"Correspondents\": \"09a89901-d2f1-4778-befe-ebab57659b90\", \"OriginalChannel\": \"EMAIL\", \"DateOfCorrespondence\": \"2019-04-23\"}, \"type\": \"MIN\", \"uuid\": \"3e5cf44f-e86a-4b21-891a-018e2343cda1\", \"created\": \"2019-04-23T12:57:19.738532\", \"reference\": \"MIN/0120101/19\", \"caseDeadline\": \"2019-05-22\", \"dateReceived\": \"2019-04-23\", \"primaryTopic\": null, \"primaryCorrespondent\": \"09a89901-d2f1-4778-befe-ebab57659b90\"}", "an-env", LocalDateTime.parse("2019-04-23 12:58:04",dateFormatter), "CASE_UPDATED", UUID.randomUUID().toString());
        deleted.setDeleted(true);
        return new LinkedHashSet<AuditEvent>(){{
            add(new AuditEvent(UUID.fromString("3e5cf44f-e86a-4b21-891a-018e2343cda1"),UUID.randomUUID(),UUID.randomUUID().toString(),"a-service", "{\"data\": {\"valid\": \"true\", \"DateReceived\": \"2019-04-23\", \"CopyNumberTen\": \"FALSE\", \"Correspondents\": \"09a89901-d2f1-4778-befe-ebab57659b90\", \"OriginalChannel\": \"EMAIL\", \"DateOfCorrespondence\": \"2019-04-23\"}, \"type\": \"MIN\", \"uuid\": \"3e5cf44f-e86a-4b21-891a-018e2343cda1\", \"created\": \"2019-04-23T12:57:19.738532\", \"reference\": \"MIN/0120101/19\", \"caseDeadline\": \"2019-05-22\", \"dateReceived\": \"2019-04-23\", \"primaryTopic\": null, \"primaryCorrespondent\": \"09a89901-d2f1-4778-befe-ebab57659b90\"}", "an-env", LocalDateTime.parse("2019-04-24 12:58:04",dateFormatter), "CASE_UPDATED", UUID.randomUUID().toString()));
            add(new AuditEvent(UUID.fromString("3e5cf44f-e86a-4b21-891a-018e2343cda1"),UUID.randomUUID(),UUID.randomUUID().toString(),"a-service", "{\"data\": {\"valid\": \"true\", \"DateReceived\": \"2019-04-23\", \"CopyNumberTen\": \"FALSE\", \"Correspondents\": \"09a89901-d2f1-4778-befe-ebab57659b90\", \"OriginalChannel\": \"EMAIL\", \"DateOfCorrespondence\": \"2019-04-23\"}, \"type\": \"MIN\", \"uuid\": \"3e5cf44f-e86a-4b21-891a-018e2343cda1\", \"created\": \"2019-04-23T12:57:19.738532\", \"reference\": \"MIN/0120101/19\", \"caseDeadline\": \"2019-05-22\", \"dateReceived\": \"2019-04-23\", \"primaryTopic\": null, \"primaryCorrespondent\": \"09a89901-d2f1-4778-befe-ebab57659b90\"}", "an-env", LocalDateTime.parse("2019-04-23 12:58:04",dateFormatter), "CASE_UPDATED", UUID.randomUUID().toString()));
            add(new AuditEvent(UUID.fromString("3e5cf44f-e86a-4b21-891a-018e2343cda0"),UUID.randomUUID(),UUID.randomUUID().toString(),"a-service", "{\"data\": {\"valid\": \"true\", \"DateReceived\": \"2019-04-23\", \"CopyNumberTen\": \"FALSE\", \"Correspondents\": \"09a89901-d2f1-4778-befe-ebab57659b90\", \"OriginalChannel\": \"EMAIL\", \"DateOfCorrespondence\": \"2019-04-23\"}, \"type\": \"MIN\", \"uuid\": \"3e5cf44f-e86a-4b21-891a-018e2343cda1\", \"created\": \"2019-04-23T12:57:19.738532\", \"reference\": \"MIN/0120101/19\", \"caseDeadline\": \"2019-05-22\", \"dateReceived\": \"2019-04-23\", \"primaryTopic\": null, \"primaryCorrespondent\": \"09a89901-d2f1-4778-befe-ebab57659b90\"}", "an-env", LocalDateTime.parse("2019-04-23 12:58:04",dateFormatter), "CASE_UPDATED", UUID.randomUUID().toString()));
            add(new AuditEvent(UUID.fromString("3e5cf44f-e86a-4b21-891a-018e2343cda1"),UUID.randomUUID(),UUID.randomUUID().toString(),"a-service", "{\"type\": \"MIN\", \"uuid\": \"3e5cf44f-e86a-4b21-891a-018e2343cda1\", \"created\": \"2019-04-23T09:18:26.446343\", \"reference\": \"MIN/0120091/19\", \"caseDeadline\": \"2019-05-22\", \"dateReceived\": \"2019-04-23\"}", "an-env", LocalDateTime.parse("2019-04-23 09:18:26", dateFormatter), "CASE_CREATED", UUID.randomUUID().toString()));
            add(new AuditEvent(UUID.fromString("3e5cf44f-e86a-4b21-891a-018e2343cda1"),UUID.randomUUID(),UUID.randomUUID().toString(),"a-service", "{\"type\": \"MIN\", \"uuid\": \"3e5cf44f-e86a-4b21-891a-018e2343cda1\"}", "an-env", LocalDateTime.parse("2019-04-23 15:20:16", dateFormatter), "CASE_COMPLETED", UUID.randomUUID().toString()));
            add(deleted);
        }};
    }

    @Test
    public void shouldAllowValuesInAllowList() {
        // no exception expected
        repository.checkViewNameIsAllowed("allow1");
        repository.checkViewNameIsAllowed("Allow1");
        repository.checkViewNameIsAllowed("ALLOW2");
    }

    @Test
    public void shouldThrowErrorWhenValuesNotInAllowList() {
        assertThatThrownBy(() -> {
            repository.checkViewNameIsAllowed("not allowed");
        }).isInstanceOf(SecurityException.class);
    }


}