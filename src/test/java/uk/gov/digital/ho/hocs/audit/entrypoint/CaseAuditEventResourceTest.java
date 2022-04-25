package uk.gov.digital.ho.hocs.audit.entrypoint;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.hocs.audit.entrypoint.dto.DeleteCaseAuditDto;
import uk.gov.digital.ho.hocs.audit.entrypoint.dto.DeleteCaseAuditResponse;
import uk.gov.digital.ho.hocs.audit.service.AuditEventService;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CaseAuditEventResourceTest {

    @Mock
    private AuditEventService auditService;

    private final UUID caseUUID = UUID.randomUUID();

    private CaseAuditEventResource auditResource;

    @Before
    public void setUp() {
        auditResource = new CaseAuditEventResource(auditService);
    }


    @Test
    public void shouldDeleteCaseAudit() {
        DeleteCaseAuditDto deleteCaseAuditDto = new DeleteCaseAuditDto("1", true);
        when(auditService.deleteCaseAudit(caseUUID, true)).thenReturn(2);

        ResponseEntity<DeleteCaseAuditResponse> response = auditResource.deleteCaseAudit(caseUUID, deleteCaseAuditDto);

        verify(auditService).deleteCaseAudit(caseUUID, true);
        verifyNoMoreInteractions(auditService);
        assertThat(response).isNotNull();
        assertThat(response.getBody().getAuditCount()).isEqualTo(2);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

}
