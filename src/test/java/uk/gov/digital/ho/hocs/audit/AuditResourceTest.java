package uk.gov.digital.ho.hocs.audit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.cglib.core.Local;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.hocs.audit.auditdetails.dto.CreateAuditDto;
import uk.gov.digital.ho.hocs.audit.auditdetails.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.audit.auditdetails.model.AuditData;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AuditResourceTest {

    @Mock
    private AuditDataService auditService;

    private AuditDataResource auditResource;

    @Before
    public void setUp() {
        auditResource = new AuditDataResource(auditService);
    }

    @Test
    public void shouldCreateAuditWithValidParams() throws EntityCreationException {

        String correlationID, raisingService, auditPayload, namespace, type, userID;
        correlationID = "correlationID1";
        raisingService = "raisingServiceName";
        auditPayload = "";
        namespace = "namespaceEventOccurredIn";
        LocalDateTime auditTimestamp = LocalDateTime.now();
        type = "eventAuditType";
        userID = "userXYZ";

        CreateAuditDto request = new CreateAuditDto(correlationID, raisingService, auditPayload, namespace, auditTimestamp, type, userID);

        AuditData auditData = new AuditData(correlationID, raisingService, auditPayload, namespace, auditTimestamp, type, userID);

        when(auditService.createAudit(any())).thenReturn(auditData);

        ResponseEntity response = auditResource.createAudit(request);

        verify(auditService, times(1)).createAudit(any());

        verifyNoMoreInteractions(auditService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
