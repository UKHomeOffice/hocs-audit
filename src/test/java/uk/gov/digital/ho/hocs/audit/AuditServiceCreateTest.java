package uk.gov.digital.ho.hocs.audit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.digital.ho.hocs.audit.auditdetails.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.audit.auditdetails.model.AuditData;
import uk.gov.digital.ho.hocs.audit.auditdetails.repository.AuditRepository;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuditServiceCreateTest {
    
    private String correlationID = "correlationIDTest";
    private String raisingService = "testRaisingService";
    private String auditPayload = "{\"name1\":\"value1\",\"name2\":\"value2\"}";
    private String namespace = "namespaceEventOccurredIn";
    private LocalDateTime dateTime = LocalDateTime.now();
    private String auditType = "testAuditType";
    private String userID = "testUser";
    
    @Mock
    private AuditRepository auditRepository;

    @Mock
    private AuditDataService auditService;

    @BeforeEach
    public void setUp() {
        this.auditService = new AuditDataService(auditRepository);
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

    @Test
    public void shouldNotCreateAuditWhenCorrelationIDIsNullException() {

        Assertions.assertThrows(EntityCreationException.class,() ->
            auditService.createAudit(null,
                    raisingService,
                    auditPayload,
                    namespace,
                    dateTime,
                    auditType,
                    userID)
        );
    }

    @Test
    public void shouldNotCreateAuditWhenCorrelationnIDIsNull() {
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

    @Test
    public void shouldNotCreateAuditWhenRaisingServiceIsNullException() {
        Assertions.assertThrows(EntityCreationException.class,() ->
            auditService.createAudit(correlationID,
                    null,
                    auditPayload,
                    namespace,
                    dateTime,
                    auditType,
                    userID)
        );
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


    @Test
    public void shouldNotCreateAuditWhenNamespaceIsNullException() {
        Assertions.assertThrows(EntityCreationException.class,() ->
            auditService.createAudit(correlationID,
                    raisingService,
                    auditPayload,
                    null,
                    dateTime,
                    auditType,
                    userID)
        );
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

    @Test
    public void shouldNotCreateAuditWhenTimestampIsNullException() {
        Assertions.assertThrows(EntityCreationException.class,() ->
            auditService.createAudit(correlationID,
                    raisingService,
                    auditPayload,
                    namespace,
                    null,
                    auditType,
                    userID)
        );
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

    @Test
    public void shouldNotCreateAuditWhenTypeIsNullException() {
        Assertions.assertThrows(EntityCreationException.class,() ->
            auditService.createAudit(correlationID,
                    raisingService,
                    auditPayload,
                    namespace,
                    dateTime,
                    null,
                    userID)
        );
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

    @Test
    public void shouldNotCreateAuditWhenUserIDIsNullException() {

        Assertions.assertThrows(EntityCreationException.class,() ->
        auditService.createAudit(correlationID,
                raisingService,
                auditPayload,
                namespace,
                dateTime,
                auditType,
                null)
        );
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
        auditService.createAudit(correlationID,
                raisingService,
                "\"Test name\" \"Test value\"",
                namespace,
                dateTime,
                auditType,
                userID);
        verify(auditRepository, times(1)).save(any(AuditData.class));
        verifyNoMoreInteractions(auditRepository);

    }

    @Test
    public void shouldCreateAuditWhenAuditPayloadIsEmpty() {
        auditService.createAudit(correlationID,
                raisingService,
                "",
                namespace,
                dateTime,
                auditType,
                userID);
        verify(auditRepository, times(1)).save(any(AuditData.class));
        verifyNoMoreInteractions(auditRepository);

    }

    @Test
    public void shouldCreateAuditWhenAuditPayloadIsNull() {
        auditService.createAudit(correlationID,
                raisingService,
                null,
                namespace,
                dateTime,
                auditType,
                userID);
        verify(auditRepository, times(1)).save(any(AuditData.class));
        verifyNoMoreInteractions(auditRepository);

    }

}
