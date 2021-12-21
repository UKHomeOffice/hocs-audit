package uk.gov.digital.ho.hocs.audit.auditdetails.dto;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class DeleteCaseAuditResponseTest {

    @Test
    public void fromReturnsPopulatedResponse(){
        UUID caseUUID = UUID.randomUUID();
        DeleteCaseAuditDto deleteCaseAuditDto = new DeleteCaseAuditDto("correlationId", true);

        DeleteCaseAuditResponse deleteCaseAuditResponse = DeleteCaseAuditResponse.from(caseUUID, deleteCaseAuditDto, 123);

        assertThat(deleteCaseAuditResponse.getCorrelationID()).isEqualTo("correlationId");
        assertThat(deleteCaseAuditResponse.getCaseUUID()).isEqualTo(caseUUID);
        assertThat(deleteCaseAuditResponse.getDeleted()).isTrue();
        assertThat(deleteCaseAuditResponse.getAuditCount()).isEqualTo(123);
    }
}
