package uk.gov.digital.ho.hocs.audit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.audit.auditdetails.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.audit.auditdetails.model.AuditData;
import uk.gov.digital.ho.hocs.audit.auditdetails.repository.AuditRepository;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AuditServiceTest {
    
    private String correlationID;
    private String raisingService;
    private String auditPayload;
    private String namespace;
    private LocalDateTime dateTime;
    private String auditType;
    private String userID;
    
    @Mock
    private AuditRepository auditRepository;

    @Mock
    private AuditDataService auditService;

    @Before
    public void setUp() {
        this.auditService = new AuditDataService(auditRepository);
        correlationID = "correlationIDTest";
        raisingService = "testRaisingService";
        auditPayload = "{\"name1\":\"value1\",\"name2\":\"value2\"}";
        namespace = "namespaceEventOccurredIn";
        dateTime = LocalDateTime.now();
        auditType = "testAuditType";
        userID = "testUser";
    }

    @Test
    public void shouldCreateAuditWithValidParams() {

        auditService.createAudit(correlationID,
                raisingService,
                auditPayload,
                namespace,
                dateTime,
                auditType,
                userID);

        verify(auditRepository, times(1)).save(any(AuditData.class));
        verifyNoMoreInteractions(auditRepository);
    }

    @Test(expected = EntityCreationException.class)
    public void shouldNotCreateAuditWhencorrelationnIDIsNullException() {
        auditService.createAudit(null,
                raisingService,
                auditPayload,
                namespace,
                dateTime,
                auditType,
                userID);
    }

    @Test
    public void shouldNotCreateAuditWhencorrelationnIDIsNull() {
        try {
            auditService.createAudit(null,
                    raisingService,
                    auditPayload,
                    namespace,
                    dateTime,
                    auditType,
                    userID);
        } catch (EntityCreationException e){
            //Do Nothing
        }
        verifyNoMoreInteractions(auditRepository);
    }

    @Test(expected = EntityCreationException.class)
    public void shouldNotCreateAuditWhenRaisingServiceIsNullException() {
        auditService.createAudit(correlationID,
                null,
                auditPayload,
                namespace,
                dateTime,
                auditType,
                userID);
    }

    @Test
    public void shouldNotCreateAuditWhenRaisingServiceIsNull() {
        try {
            auditService.createAudit(correlationID,
                    null,
                    auditPayload,
                    namespace,
                    dateTime,
                    auditType,
                    userID);
        } catch (EntityCreationException e){
            //Do Nothing
        }
        verifyNoMoreInteractions(auditRepository);
    }


    @Test(expected = EntityCreationException.class)
    public void shouldNotCreateAuditWhenNamespaceIsNullException() {
        auditService.createAudit(correlationID,
                raisingService,
                auditPayload,
                null,
                dateTime,
                auditType,
                userID);
    }

    @Test
    public void shouldNotCreateAuditWhenNamespaceIsNull() {
        try {
            auditService.createAudit(correlationID,
                    raisingService,
                    auditPayload,
                    null,
                    dateTime,
                    auditType,
                    userID);
        } catch (EntityCreationException e){
            //Do Nothing
        }
        verifyNoMoreInteractions(auditRepository);
    }

    @Test(expected = EntityCreationException.class)
    public void shouldNotCreateAuditWhenTimestampIsNullException() {
        auditService.createAudit(correlationID,
                raisingService,
                auditPayload,
                namespace,
                null,
                auditType,
                userID);
    }

    @Test
    public void shouldNotCreateAuditWhenTimestampIsNull() {
        try {
            auditService.createAudit(correlationID,
                    raisingService,
                    auditPayload,
                    namespace,
                    null,
                    auditType,
                    userID);
        } catch (EntityCreationException e){
            //Do Nothing
        }
        verifyNoMoreInteractions(auditRepository);
    }

    @Test(expected = EntityCreationException.class)
    public void shouldNotCreateAuditWhenTypeIsNullException() {
        auditService.createAudit(correlationID,
                raisingService,
                auditPayload,
                namespace,
                dateTime,
                null,
                userID);
    }

    @Test
    public void shouldNotCreateAuditWhenTypeIsNull() {
        try {

            auditService.createAudit(correlationID,
                    raisingService,
                    auditPayload,
                    namespace,
                    dateTime,
                    null,
                    userID);
        } catch (EntityCreationException e){
            //Do Nothing
        }
        verifyNoMoreInteractions(auditRepository);
    }

    @Test(expected = EntityCreationException.class)
    public void shouldNotCreateAuditWhenUserIDIsNullException() {

        auditService.createAudit(correlationID,
                raisingService,
                auditPayload,
                namespace,
                dateTime,
                auditType,
                null);
    }

    @Test
    public void shouldNotCreateAuditWhenUserIDIsNull() {
        try {
            auditService.createAudit(correlationID,
                    raisingService,
                    auditPayload,
                    namespace,
                    dateTime,
                    auditType,
                    null);
        } catch (EntityCreationException e) {
            //Do Nothing
        }
        verifyNoMoreInteractions(auditRepository);
    }

    @Test
    public void shouldCreateAuditWhenAuditPayloadIsInvalid() {
        AuditData auditData = auditService.createAudit(correlationID,
                raisingService,
                "\"Test name\" \"Test value\"",
                namespace,
                dateTime,
                auditType,
                userID);
        verify(auditRepository, times(1)).save(any(AuditData.class));
        verifyNoMoreInteractions(auditRepository);

//        assertThat(auditData.getcorrelationnID()).isEqualTo(correlationID);
//        assertThat(auditData.getRaisingService()).isEqualTo(raisingService);
//        assertThat(auditData.getAuditPayload()).isEqualTo(auditPayload);
//        assertThat(auditData.getNamespace()).isEqualTo(namespace);
//        assertThat(auditData.getType()).isEqualTo(auditType);
//        assertThat(auditData.getUserID()).isEqualTo(userID);
    }

}
