package uk.gov.digital.ho.hocs.audit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.audit.auditdetails.model.AuditData;
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
    private AuditDataService auditService;

    @BeforeEach
    public void setUp() {
        this.auditService = new AuditDataService(auditRepository);
    }

    @Test
    public void deleteCaseAuditWhenTrueUpdatedToTrue() {

        UUID caseUUID = UUID.randomUUID();
        AuditData auditData = new AuditData(correlationID,raisingService,auditPayload,namespace,dateTime,auditType,userID);
        ArrayList auditDatas = new ArrayList() {{
            add(auditData);
        }};
        when(auditRepository.findAuditDataByCaseUUID(caseUUID)).thenReturn(auditDatas);

        auditService.deleteCaseAudit(caseUUID, true);

        verify(auditRepository).findAuditDataByCaseUUID(caseUUID);
        verify(auditRepository).save(auditData);
        verifyNoMoreInteractions(auditRepository);
    }
}
