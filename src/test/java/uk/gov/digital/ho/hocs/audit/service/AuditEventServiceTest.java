package uk.gov.digital.ho.hocs.audit.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.audit.core.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.audit.core.utils.JsonValidator;
import uk.gov.digital.ho.hocs.audit.repository.AuditRepository;
import uk.gov.digital.ho.hocs.audit.repository.entity.AuditEvent;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Stream;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuditEventServiceTest {

    private final String correlationID = "correlationIDTest";
    private final String raisingService = "testRaisingService";
    private final String auditPayload = "{\"name1\":\"value1\",\"name2\":\"value2\"}";
    private final String namespace = "namespaceEventOccurredIn";
    private final LocalDateTime dateTime = LocalDateTime.now();
    private final String auditType = "testAuditType";
    private final String userID = "testUser";

    @Mock
    private AuditRepository auditRepository;

    @Mock
    private AuditEventService auditService;

    @Mock
    private JsonValidator jsonValidator;

    @Mock
    private EntityManager entityManager;

    @Before
    public void setUp() {
        this.auditService = new AuditEventService(auditRepository, jsonValidator, entityManager);
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

        verify(auditRepository, times(1)).save(any(AuditEvent.class));
        verifyNoMoreInteractions(auditRepository);
    }

    @Test(expected = EntityCreationException.class)
    public void shouldNotCreateAuditWhenCorrelationnIDIsNullException() {
        auditService.createAudit(null,
                raisingService,
                auditPayload,
                namespace,
                dateTime,
                auditType,
                userID);
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
        auditService.createAudit(correlationID,
                raisingService,
                "\"Test name\" \"Test value\"",
                namespace,
                dateTime,
                auditType,
                userID);
        verify(auditRepository, times(1)).save(any(AuditEvent.class));
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
        verify(auditRepository, times(1)).save(any(AuditEvent.class));
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
        verify(auditRepository, times(1)).save(any(AuditEvent.class));
        verifyNoMoreInteractions(auditRepository);

    }

    @Test
    public void shouldGetAuditForCase() {

        UUID caseUUID = UUID.randomUUID();
        String[] typesArray = {"TYPE1", "TYPE2"};

        var auditData = Stream.of(new AuditEvent(correlationID, raisingService, auditPayload, namespace, dateTime, auditType, userID));

        when(auditRepository.findAuditDataByCaseUUIDAndTypesIn(caseUUID, typesArray)).thenReturn(auditData);
        auditService.getAuditDataByCaseUUID(caseUUID, typesArray);

        verify(auditRepository, times(1)).findAuditDataByCaseUUIDAndTypesIn(caseUUID, typesArray);
        verifyNoMoreInteractions(auditRepository);
    }

    @Test
    public void shouldGetAuditForCaseFromDate() {

        UUID caseUUID = UUID.randomUUID();
        String[] typesArray = {"TYPE1", "TYPE2"};
        LocalDate from = LocalDate.of(2022, 4, 1);

        var auditData = Stream.of(new AuditEvent(correlationID, raisingService, auditPayload, namespace, dateTime, auditType, userID));

        when(auditRepository.findAuditDataByCaseUUIDAndTypesInAndFrom(caseUUID, typesArray, from)).thenReturn(auditData);
        auditService.getAuditDataByCaseUUID(caseUUID, typesArray, from);

        verify(auditRepository, times(1)).findAuditDataByCaseUUIDAndTypesInAndFrom(caseUUID, typesArray, from);
        verifyNoMoreInteractions(auditRepository);
    }

    @Test
    public void deleteCaseAuditWhenTrueUpdatedToTrue() {

        UUID caseUUID = UUID.randomUUID();
        AuditEvent auditEvent = new AuditEvent(correlationID, raisingService, auditPayload, namespace, dateTime, auditType, userID);
        ArrayList auditDatas = new ArrayList() {{
            add(auditEvent);
        }};
        when(auditRepository.findAuditDataByCaseUUID(caseUUID)).thenReturn(auditDatas);

        auditService.deleteCaseAudit(caseUUID, true);

        verify(auditRepository).findAuditDataByCaseUUID(caseUUID);
        verify(auditRepository).save(auditEvent);
        verifyNoMoreInteractions(auditRepository);
    }

}
