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
        CreateAuditDto request = new CreateAuditDto("correlationID1",
                "raisingServiceName",
                "",
                "namespaceEventOccurredIn",
                "eventAuditType",
                "userXYZ");

        auditService.createAudit(request);

        verify(auditRepository, times(1)).save(any(AuditData.class));
        verifyNoMoreInteractions(auditRepository);
    }

    @Test
    public void shouldConvertDTOToAuditData() {

        CreateAuditDto request = new CreateAuditDto("correlationID1",
                "raisingServiceName",
                "",
                "namespaceEventOccurredIn",
                "eventAuditType",
                "userXYZ");

        AuditData auditData = AuditData.fromDto(request);

        assertThat(auditData.getCorrelationID()).isEqualTo("correlationID1");
        assertThat(auditData.getRaisingService()).isEqualTo("raisingServiceName");
        assertThat(auditData.getAuditPayload()).isEqualTo("");
        assertThat(auditData.getNamespace()).isEqualTo("namespaceEventOccurredIn");
        assertThat(auditData.getType()).isEqualTo("eventAuditType");
        assertThat(auditData.getUserID()).isEqualTo("userXYZ");
    }

    @Test(expected = EntityCreationException.class)
    public void shouldNotCreateAuditWhenCorrelationIDIsNullException() {
        CreateAuditDto request = new CreateAuditDto(null,
                "raisingServiceName",
                "",
                "namespaceEventOccurredIn",
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
                "eventAuditType",
                null);
        auditService.createAudit(request);
    }

    @Test
    public void shouldNotCreateAuditWhenAuditPayloadIsInvalid() {
        CreateAuditDto request = new CreateAuditDto("correlationID1",
                "raisingServiceName",
                "\"Test name\" \"Test value\"",
                "namespaceEventOccurredIn",
                "eventAuditType",
                null);
        try {
            auditService.createAudit(request);
        } catch (EntityCreationException e) {
            //Do Nothing
        }
        verifyNoMoreInteractions(auditRepository);
    }
}
