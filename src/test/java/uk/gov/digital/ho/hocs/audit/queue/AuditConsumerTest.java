package uk.gov.digital.ho.hocs.audit.queue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.audit.AuditDataService;
import uk.gov.digital.ho.hocs.audit.auditdetails.dto.CreateAuditDto;
import uk.gov.digital.ho.hocs.audit.auditdetails.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.audit.auditdetails.exception.EntityNotFoundException;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AuditConsumerTest extends CamelTestSupport {

    private static final String auditQueue = "direct:reporting-queue";
    private static final String dlq = "mock:reporting-queue-dlq";
    private ObjectMapper mapper;

    @Mock
    private AuditDataService mockDataService;

    @Before
    public void setup(){
        mapper = new ObjectMapper();
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new AuditConsumer(mockDataService, auditQueue, dlq, 0,0,0);

    }
    @Test
    public void shouldCallAddDocumentToAuditService() throws JsonProcessingException, EntityCreationException, EntityNotFoundException {

        CreateAuditDto auditDto = buildAuditDto();
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

        CreateAuditDto auditDto = buildAuditDto();

        doThrow(EntityCreationException.class)
                .when(mockDataService).createAudit(any());
        getMockEndpoint(dlq).setExpectedCount(1);
        String json = mapper.writeValueAsString(auditDto);
        template.sendBody(auditQueue, json);
        getMockEndpoint(dlq).assertIsSatisfied();
    }

    private CreateAuditDto buildAuditDto(){
        return new CreateAuditDto("correlationIDTest",
                "testRaisingService",
                "{\"name1\":\"value1\",\"name2\":\"value2\"}",
                "namespaceEventOccurredIn",
                LocalDateTime.now(),
                "testAuditType",
                "testUser");
    }
}