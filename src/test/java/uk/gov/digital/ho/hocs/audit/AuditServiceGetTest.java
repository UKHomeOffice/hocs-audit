package uk.gov.digital.ho.hocs.audit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.digital.ho.hocs.audit.auditdetails.model.AuditData;
import uk.gov.digital.ho.hocs.audit.auditdetails.repository.AuditRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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
    private AuditDataService auditService;

    @BeforeEach
    public void setUp() {
        this.auditService = new AuditDataService(auditRepository);
    }

    @Test
    public void shouldGetAuditForCase() {

        UUID caseUUID = UUID.randomUUID();
        String types = "TYPE1,TYPE2";
        String[] typesArray = {"TYPE1","TYPE2"};

        ArrayList auditData = new ArrayList() {{
            add(new AuditData(correlationID,raisingService,auditPayload,namespace,dateTime,auditType,userID));
        }};

        when(auditRepository.findAuditDataByCaseUUIDAndTypesIn(caseUUID, typesArray)).thenReturn(auditData);
        auditService.getAuditDataByCaseUUID(caseUUID, types);

        verify(auditRepository, times(1)).findAuditDataByCaseUUIDAndTypesIn(caseUUID, typesArray);
        verifyNoMoreInteractions(auditRepository);
    }
}
