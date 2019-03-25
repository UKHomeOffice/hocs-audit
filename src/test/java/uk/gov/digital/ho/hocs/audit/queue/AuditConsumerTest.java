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

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AuditConsumerTest extends CamelTestSupport {

    private String auditQueue = "direct:reporting-queue";
    private String dlq = "mock:reporting-queue-dlq";
    private ObjectMapper mapper;

    private String correlationID;
    private UUID caseUUID;
    private UUID stageUUID;
    private String raisingService;
    private String auditPayload;
    private String namespace;
    private LocalDateTime auditTimestamp;
    private String type;
    private String userID;

    @Mock
    private AuditDataService mockDataService;

    @Before
    public void setUpTest(){
        mapper = new ObjectMapper();
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        context.setStreamCaching(true);
        correlationID = "correlationIDTest";
        caseUUID = UUID.randomUUID();
        stageUUID = UUID.randomUUID();
        raisingService = "testRaisingService";
        auditPayload = "{\"name1\":\"value1\",\"name2\":\"value2\"}";
        namespace = "namespaceEventOccurredIn";
        auditTimestamp = LocalDateTime.now();
        type = "testAuditType";
        userID = "testUser";
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new AuditConsumer(mockDataService, auditQueue, dlq, 0,0,0);
    }

    @Test
    public void shouldCallAddAuditToAuditService() throws JsonProcessingException {

        CreateAuditDto auditDto = new CreateAuditDto(correlationID, raisingService, auditPayload, namespace, auditTimestamp, type, userID);
        String json = mapper.writeValueAsString(auditDto);
        template.sendBody(auditQueue, json);
        verify(mockDataService, times(1)).createAudit(null, null, correlationID, raisingService, auditPayload, namespace, auditTimestamp, type, userID);
        verifyNoMoreInteractions(mockDataService);
    }

    @Test
    public void shouldCallAddAuditToAuditServiceCaseUUID() throws JsonProcessingException {

        CreateAuditDto auditDto = new CreateAuditDto(caseUUID, stageUUID, correlationID, raisingService, auditPayload, namespace, auditTimestamp, type, userID);
        String json = mapper.writeValueAsString(auditDto);
        template.sendBody(auditQueue, json);
        verify(mockDataService, times(1)).createAudit(caseUUID, stageUUID, correlationID, raisingService, auditPayload, namespace, auditTimestamp, type, userID);
        verifyNoMoreInteractions(mockDataService);
    }

    @Test
    public void shouldNotProcessMessageWhenMarshallingFails() throws JsonProcessingException, InterruptedException {
        getMockEndpoint(dlq).setExpectedCount(1);
        String json = mapper.writeValueAsString("{invalid:invalid}");
        template.sendBody(auditQueue, json);
        verify(mockDataService, never()).createAudit(correlationID, raisingService, auditPayload, namespace, auditTimestamp, type, userID);
        getMockEndpoint(dlq).assertIsSatisfied();
    }

    @Test
    public void shouldTransferToDLQOnFailure() throws JsonProcessingException, InterruptedException {

        CreateAuditDto auditDto = new CreateAuditDto(correlationID, raisingService, auditPayload, namespace, auditTimestamp,type,userID);

        doThrow(EntityCreationException.class)
                .when(mockDataService).createAudit(null, null, correlationID, raisingService, auditPayload, namespace, auditTimestamp, type, userID);
        getMockEndpoint(dlq).setExpectedCount(1);
        String json = mapper.writeValueAsString(auditDto);
        template.sendBody(auditQueue, json);
        getMockEndpoint(dlq).assertIsSatisfied();
    }

}