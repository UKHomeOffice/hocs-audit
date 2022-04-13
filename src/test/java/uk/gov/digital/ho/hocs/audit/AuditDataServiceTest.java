package uk.gov.digital.ho.hocs.audit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.audit.auditdetails.model.AuditEvent;
import uk.gov.digital.ho.hocs.audit.auditdetails.repository.AuditRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(MockitoJUnitRunner.class)
public class AuditDataServiceTest {

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
    public void deleteCaseAuditWhenTrueUpdatedToTrue() {

        UUID caseUUID = UUID.randomUUID();
        AuditEvent auditEvent = new AuditEvent(correlationID,raisingService,auditPayload,namespace,dateTime,auditType,userID);
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
