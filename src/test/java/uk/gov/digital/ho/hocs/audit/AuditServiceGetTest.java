package uk.gov.digital.ho.hocs.audit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.audit.auditdetails.model.AuditEvent;
import uk.gov.digital.ho.hocs.audit.auditdetails.repository.AuditRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AuditServiceGetTest {
    
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
    private AuditEventService auditService;

    @Before
    public void setUp() {
        this.auditService = new AuditEventService(auditRepository);
    }

    @Test
    public void shouldGetAuditForCase() {

        UUID caseUUID = UUID.randomUUID();
        String types = "TYPE1,TYPE2";
        String[] typesArray = {"TYPE1","TYPE2"};

        ArrayList auditData = new ArrayList() {{
            add(new AuditEvent(correlationID,raisingService,auditPayload,namespace,dateTime,auditType,userID));
        }};

        when(auditRepository.findAuditDataByCaseUUIDAndTypesIn(caseUUID, typesArray)).thenReturn(auditData);
        auditService.getAuditDataByCaseUUID(caseUUID, types);

        verify(auditRepository, times(1)).findAuditDataByCaseUUIDAndTypesIn(caseUUID, typesArray);
        verifyNoMoreInteractions(auditRepository);
    }

    @Test
    public void shouldGetAuditForCaseFromDate() {

        UUID caseUUID = UUID.randomUUID();
        String types = "TYPE1,TYPE2";
        String[] typesArray = {"TYPE1","TYPE2"};
        LocalDate from = LocalDate.of(2022,4,1);

        ArrayList auditData = new ArrayList() {{
            add(new AuditEvent(correlationID,raisingService,auditPayload,namespace,dateTime,auditType,userID));
        }};

        when(auditRepository.findAuditDataByCaseUUIDAndTypesInAndFrom(caseUUID, typesArray, from)).thenReturn(auditData);
        auditService.getAuditDataByCaseUUID(caseUUID, types, from);

        verify(auditRepository, times(1)).findAuditDataByCaseUUIDAndTypesInAndFrom(caseUUID, typesArray, from);
        verifyNoMoreInteractions(auditRepository);
    }
}
