package uk.gov.digital.ho.hocs.audit.auditdetails.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
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
