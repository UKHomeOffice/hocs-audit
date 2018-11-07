package uk.gov.digital.ho.hocs.audit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.audit.auditdetails.dto.CreateAuditDto;
import uk.gov.digital.ho.hocs.audit.auditdetails.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.audit.auditdetails.model.AuditData;
import uk.gov.digital.ho.hocs.audit.auditdetails.repository.AuditRepository;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AuditServiceTest {

    @Mock
    private AuditRepository auditRepository;

    @Mock
    private AuditDataService auditService;

    @Before
    public void setUp() {
        this.auditService = new AuditDataService(auditRepository);
    }

    @Test
    public void shouldCreateAuditWithValidParams() {
        CreateAuditDto request = buildValidAuditDto();

        auditService.createAudit(request);

        verify(auditRepository, times(1)).save(any(AuditData.class));
        verifyNoMoreInteractions(auditRepository);
    }

    @Test
    public void shouldConvertDTOToAuditData() {

        CreateAuditDto request = buildValidAuditDto();

        AuditData auditData = AuditData.fromDto(request);

        assertThat(auditData.getCorrelationID()).isEqualTo("correlationIDTest");
        assertThat(auditData.getRaisingService()).isEqualTo("testRaisingService");
        assertThat(auditData.getAuditPayload()).isEqualTo("{\"name1\":\"value1\",\"name2\":\"value2\"}");
        assertThat(auditData.getNamespace()).isEqualTo("namespaceEventOccurredIn");
        assertThat(auditData.getType()).isEqualTo("testAuditType");
        assertThat(auditData.getUserID()).isEqualTo("testUser");
    }

    @Test(expected = EntityCreationException.class)
    public void shouldNotCreateAuditWhenCorrelationIDIsNullException() {
        CreateAuditDto request = new CreateAuditDto(null,
                "raisingServiceName",
                "",
                "namespaceEventOccurredIn",
                LocalDateTime.now(),
                "eventAuditType",
                "userXYZ");
        auditService.createAudit(request);
    }

    @Test
    public void shouldNotCreateAuditWhenCorrelationIDIsNull() {
        CreateAuditDto request = new CreateAuditDto(null,
                "raisingServiceName",
                "",
                "namespaceEventOccurredIn",
                LocalDateTime.now(),
                "eventAuditType",
                "userXYZ");
        try {
            auditService.createAudit(request);
        } catch (EntityCreationException e){
            //Do Nothing
        }
        verifyNoMoreInteractions(auditRepository);
    }

    @Test(expected = EntityCreationException.class)
    public void shouldNotCreateAuditWhenRaisingServiceIsNullException() {
        CreateAuditDto request = new CreateAuditDto("correlationID1",
                null,
                "",
                "namespaceEventOccurredIn",
                LocalDateTime.now(),
                "eventAuditType",
                "userXYZ");
        auditService.createAudit(request);
    }

    @Test
    public void shouldNotCreateAuditWhenRaisingServiceIsNull() {
        CreateAuditDto request = new CreateAuditDto("correlationID1",
                null,
                "",
                "namespaceEventOccurredIn",
                LocalDateTime.now(),
                "eventAuditType",
                "userXYZ");
        try {
            auditService.createAudit(request);
        } catch (EntityCreationException e){
            //Do Nothing
        }
        verifyNoMoreInteractions(auditRepository);
    }


    @Test(expected = EntityCreationException.class)
    public void shouldNotCreateAuditWhenNamespaceIsNullException() {
        CreateAuditDto request = new CreateAuditDto("correlationID1",
                "raisingServiceName",
                "",
                null,
                LocalDateTime.now(),
                "eventAuditType",
                "userXYZ");
        auditService.createAudit(request);
    }

    @Test
    public void shouldNotCreateAuditWhenNamespaceIsNull() {
        CreateAuditDto request = new CreateAuditDto("correlationID1",
                "raisingServiceName",
                "",
                null,
                LocalDateTime.now(),
                "eventAuditType",
                "userXYZ");
        try {
            auditService.createAudit(request);
        } catch (EntityCreationException e){
            //Do Nothing
        }
        verifyNoMoreInteractions(auditRepository);
    }

    @Test(expected = EntityCreationException.class)
    public void shouldNotCreateAuditWhenTimestampIsNullException() {
        CreateAuditDto request = new CreateAuditDto("correlationID1",
                "raisingServiceName",
                "",
                "namespaceEventOccurredIn",
                null,
                "eventAuditType",
                "userXYZ");
        auditService.createAudit(request);
    }

    @Test
    public void shouldNotCreateAuditWhenTimestampIsNull() {
        CreateAuditDto request = new CreateAuditDto("correlationID1",
                "raisingServiceName",
                "",
                "namespaceEventOccurredIn",
                null,
                "eventAuditType",
                "userXYZ");
        try {
            auditService.createAudit(request);
        } catch (EntityCreationException e){
            //Do Nothing
        }
        verifyNoMoreInteractions(auditRepository);
    }

    @Test(expected = EntityCreationException.class)
    public void shouldNotCreateAuditWhenTypeIsNullException() {
        CreateAuditDto request = new CreateAuditDto("correlationID1",
                "raisingServiceName",
                "",
                "namespaceEventOccurredIn",
                LocalDateTime.now(),
                null,
                "userXYZ");
        auditService.createAudit(request);
    }

    @Test
    public void shouldNotCreateAuditWhenTypeIsNull() {
        CreateAuditDto request = new CreateAuditDto("correlationID1",
                "raisingServiceName",
                "",
                "namespaceEventOccurredIn",
                LocalDateTime.now(),
                null,
                "userXYZ");
        try {
            auditService.createAudit(request);
        } catch (EntityCreationException e){
            //Do Nothing
        }
        verifyNoMoreInteractions(auditRepository);
    }

    @Test(expected = EntityCreationException.class)
    public void shouldNotCreateAuditWhenUserIDIsNullException() {
        CreateAuditDto request = new CreateAuditDto("correlationID1",
                "raisingServiceName",
                "",
                "namespaceEventOccurredIn",
                LocalDateTime.now(),
                "eventAuditType",
                null);
        auditService.createAudit(request);
    }

    @Test
    public void shouldNotCreateAuditWhenUserIDIsNull() {
        CreateAuditDto request = new CreateAuditDto("correlationID1",
                "raisingServiceName",
                "",
                "namespaceEventOccurredIn",
                LocalDateTime.now(),
                "eventAuditType",
                null);
        try {
            auditService.createAudit(request);
        } catch (EntityCreationException e) {
            //Do Nothing
        }
        verifyNoMoreInteractions(auditRepository);
    }


    @Test(expected = EntityCreationException.class)
    public void shouldNotCreateAuditWhenAuditPayloadIsInvalidException() {
        CreateAuditDto request = new CreateAuditDto("correlationID1",
                "raisingServiceName",
                "\"Test name\" \"Test value\"",
                "namespaceEventOccurredIn",
                LocalDateTime.now(),
                "eventAuditType",
                "testUser");
        auditService.createAudit(request);
    }

    @Test
    public void shouldNotCreateAuditWhenAuditPayloadIsInvalid() {
        CreateAuditDto request = new CreateAuditDto("correlationID1",
                "raisingServiceName",
                "\"Test name\" \"Test value\"",
                "namespaceEventOccurredIn",
                LocalDateTime.now(),
                "eventAuditType",
                "testUser");
        try {
            auditService.createAudit(request);
        } catch (EntityCreationException e) {
            //Do Nothing
        }
        verifyNoMoreInteractions(auditRepository);
    }

    private CreateAuditDto buildValidAuditDto(){
        return new CreateAuditDto("correlationIDTest",
                "testRaisingService",
                "{\"name1\":\"value1\",\"name2\":\"value2\"}",
                "namespaceEventOccurredIn",
                LocalDateTime.now(),
                "testAuditType",
                "testUser");
    }
}
