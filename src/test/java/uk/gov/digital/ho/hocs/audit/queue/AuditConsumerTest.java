package uk.gov.digital.ho.hocs.audit.queue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.audit.AuditDataService;
import uk.gov.digital.ho.hocs.audit.auditdetails.dto.CreateAuditDto;
import uk.gov.digital.ho.hocs.audit.auditdetails.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.audit.auditdetails.exception.EntityNotFoundException;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AuditConsumerTest extends CamelTestSupport {

    private static final String auditQueue = "direct:reporting-queue";
    private static final String dlq = "mock:reporting-queue-dlq";
    private ObjectMapper mapper = new ObjectMapper();


    @Mock
    private AuditDataService mockDataService;

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new AuditConsumer(mockDataService, auditQueue, dlq, 0,0,0);
    }
    @Test
    public void shouldCallAddDocumentToCaseService() throws JsonProcessingException, EntityCreationException, EntityNotFoundException {

        CreateAuditDto auditDto = new CreateAuditDto("correlationID1",
                "raisingServiceName",
                "",
                "namespaceEventOccurredIn",
                "eventAuditType",
                "userXYZ");

        String json = mapper.writeValueAsString(auditDto);
        template.sendBody(auditQueue, json);

        verify(mockDataService, times(1)).createAudit(any());
    }

    @Test
    public void shouldNotProcessMessgeWhenMarshellingFails() throws JsonProcessingException, InterruptedException, EntityCreationException, EntityNotFoundException {
        getMockEndpoint(dlq).setExpectedCount(1);
        String json = mapper.writeValueAsString("{invalid:invalid}");
        template.sendBody(auditQueue, json);
        verify(mockDataService, never()).createAudit(any());
        getMockEndpoint(dlq).assertIsSatisfied();
    }

    @Test
    public void shouldTransferToDLQOnFailure() throws JsonProcessingException, InterruptedException, EntityCreationException, EntityNotFoundException {

        CreateAuditDto auditDto = new CreateAuditDto("correlationID1",
                "raisingServiceName",
                "",
                "namespaceEventOccurredIn",
                "eventAuditType",
                "userXYZ");

        doThrow(EntityCreationException.class)
                .when(mockDataService).createAudit(any());

        getMockEndpoint(dlq).setExpectedCount(1);
        String json = mapper.writeValueAsString(auditDto);
        template.sendBody(auditQueue, json);
        getMockEndpoint(dlq).assertIsSatisfied();
    }

}
