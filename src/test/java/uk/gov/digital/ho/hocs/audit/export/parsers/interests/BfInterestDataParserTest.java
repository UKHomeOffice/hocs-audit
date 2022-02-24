package uk.gov.digital.ho.hocs.audit.export.parsers.interests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.digital.ho.hocs.audit.application.ZonedDateTimeConverter;
import uk.gov.digital.ho.hocs.audit.auditdetails.model.AuditData;
import uk.gov.digital.ho.hocs.audit.export.caseworkclient.CaseworkClient;
import uk.gov.digital.ho.hocs.audit.export.caseworkclient.dto.GetCaseReferenceResponse;
import uk.gov.digital.ho.hocs.audit.export.infoclient.InfoClientSupplier;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BfInterestDataParserTest {

    @MockBean
    private InfoClientSupplier infoClientSupplier;
    @MockBean
    private CaseworkClient caseworkClient;

    @SuppressWarnings("SpringJavaAutowiredMembersInspection")
    @Autowired
    private ObjectMapper objectMapper;

    private ZonedDateTimeConverter zonedDateTimeConverter;

    private final static UUID USER_UUID = UUID.randomUUID();
    private final static UUID CASE_DATA_UUID = UUID.randomUUID();
    private final static UUID STAGE_UUID = UUID.randomUUID();

    @Before
    public void before() {
        this.zonedDateTimeConverter = new ZonedDateTimeConverter(null, null);

        Mockito.when(infoClientSupplier.getUsers()).thenReturn(() -> Map.of(USER_UUID.toString(), "TEST_USER"));
        Mockito.when(caseworkClient.getCaseReference(CASE_DATA_UUID.toString()))
                .thenReturn(new GetCaseReferenceResponse(CASE_DATA_UUID, "TEST_CASE"));
    }

    @Test
    public void shouldReturnUnconvertedValuesWhenFalse() throws JsonProcessingException {
        String eventPayload =
                "{\"uuid\": \"10000000-0000-0000-0000-000000000000\", \"caseType\": \"TEST_TYPE\", " +
                        "\"eventType\": \"EXTERNAL_INTEREST_CREATED\", \"partyType\": \"PARTY_TYPE_1\"," +
                        "\"caseDataUuid\": \"00000000-0000-0000-0000-000000000000\", \"interestDetails\": \"TEST_DETAILS\"}";

        AuditData auditData = new AuditData(CASE_DATA_UUID, STAGE_UUID, UUID.randomUUID().toString(),
                "TEST_SERVICE", eventPayload , "TEST_NAMESPACE",
                LocalDateTime.of(2000,1,1,0,0), "TEST_TYPE",
                USER_UUID.toString());

        BfInterestDataParser bfInterestDataParser = new BfInterestDataParser(infoClientSupplier, caseworkClient, objectMapper,
                zonedDateTimeConverter, false);

        Assert.assertArrayEquals(new String[] { "2000-01-01T00:00:00.000000",
                        "TEST_TYPE",
                        USER_UUID.toString(),
                        CASE_DATA_UUID.toString(),
                        "PARTY_TYPE_1",
                        "TEST_DETAILS" },
                bfInterestDataParser.parsePayload(auditData));
    }

    @Test
    public void shouldReturnConvertedValuesWhenTrue() throws JsonProcessingException {
        String eventPayload =
                "{\"uuid\": \"10000000-0000-0000-0000-000000000000\", \"caseType\": \"TEST_TYPE\", " +
                        "\"eventType\": \"EXTERNAL_INTEREST_CREATED\", \"partyType\": \"PARTY_TYPE_1\"," +
                        "\"caseDataUuid\": \"00000000-0000-0000-0000-000000000000\", \"interestDetails\": \"TEST_DETAILS\"}";

        AuditData auditData = new AuditData(CASE_DATA_UUID, STAGE_UUID, UUID.randomUUID().toString(),
                "TEST_SERVICE", eventPayload , "TEST_NAMESPACE",
                LocalDateTime.of(2000,1,1,0,0), "TEST_TYPE",
                USER_UUID.toString());

        Mockito.when(infoClientSupplier.getEntityList(any())).thenReturn(() -> Map.of("PARTY_TYPE_1", "TEST_PARTY"));

        BfInterestDataParser bfInterestDataParser = new BfInterestDataParser(infoClientSupplier, caseworkClient, objectMapper,
                zonedDateTimeConverter, true);

        Assert.assertArrayEquals(new String[] { "2000-01-01T00:00:00.000000",
                        "TEST_TYPE",
                        "TEST_USER",
                        "TEST_CASE",
                        "TEST_PARTY",
                        "TEST_DETAILS" },
                bfInterestDataParser.parsePayload(auditData));
    }

}
