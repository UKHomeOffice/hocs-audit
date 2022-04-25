package uk.gov.digital.ho.hocs.audit.repository.entity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class AuditEventTest {

    @Test
    public void auditData() {
        AuditEvent auditEvent = new AuditEvent(
                "correlationId",
                "raisingService",
                "auditPayload",
                "namespace",
                LocalDateTime.now(),
                "type",
                "userId");

        assertThat(auditEvent.getDeleted()).isFalse();
    }

}
