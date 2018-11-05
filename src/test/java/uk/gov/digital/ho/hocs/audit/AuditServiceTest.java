package uk.gov.digital.ho.hocs.audit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.audit.auditdetails.dto.CreateAuditDto;
import uk.gov.digital.ho.hocs.audit.auditdetails.model.AuditData;
import uk.gov.digital.ho.hocs.audit.auditdetails.repository.AuditRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

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
        CreateAuditDto request = new CreateAuditDto( "correlationID1",
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
}
