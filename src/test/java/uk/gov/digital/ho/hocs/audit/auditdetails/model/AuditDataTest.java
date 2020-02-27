package uk.gov.digital.ho.hocs.audit.auditdetails.model;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class AuditDataTest {

    @Test
    public void auditData(){
        AuditData auditData = new AuditData(
                "correlationId",
                "raisingService",
                "auditPayload",
                "namespace",
                LocalDateTime.now(),
                "type",
                "userId");

        assertThat(auditData.getDeleted()).isFalse();
    }

}