package uk.gov.digital.ho.hocs.audit.export;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;
import static org.springframework.test.web.client.MockRestServiceServer.bindTo;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "classpath:export/beforeTest.sql", config = @SqlConfig(transactionMode = ISOLATED))
@Sql(scripts = "classpath:export/afterTest.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = AFTER_TEST_METHOD)
public class AuditExportIntTest {

    private MockRestServiceServer mockInfoService;
    private TestRestTemplate testRestTemplate = new TestRestTemplate();
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private RestTemplate restTemplate;

    @Value("classpath:export/resources/info-case-types.json")
    private Resource infoCaseTypes;

    @Value("classpath:export/resources/info-schema-MIN.json")
    private Resource infoSchemaMIN;
    private HttpHeaders headers;

    @LocalServerPort
    int port;

    @Before
    public void setup() {
        headers = new HttpHeaders();
        mockInfoService = buildMockService(restTemplate);
        setupInfoServiceCaseTypesMock();
    }

    @Test
    public void shouldGetCaseDataExportForCaseType() throws IOException {
        String caseType = "MIN";
        String dateFrom = LocalDate.of(2018, 1, 1).format(dateFormatter);
        String dateTo = LocalDate.of(2019, 6, 1).format(dateFormatter);

        setupInfoServiceSchemaMock(caseType, infoSchemaMIN);

        HttpEntity httpEntity = new HttpEntity(headers);
        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + String.format("/export?fromDate=%s&toDate=%s&caseType=%s&exportType=%s", dateFrom, dateTo, caseType, ExportType.CASE_DATA)
                , HttpMethod.GET, httpEntity, String.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getHeaders().get(HttpHeaders.CONTENT_TYPE)).contains("text/csv");
        assertThat(result.getHeaders().get(HttpHeaders.CONTENT_DISPOSITION)).contains(String.format("attachment; filename=%s-%s.csv",caseType.toLowerCase(),ExportType.CASE_DATA.toString().toLowerCase()));
        mockInfoService.verify();
    }

    @Test
    public void shouldGetValidCSVFileFromCaseDataExport() throws IOException {
        String caseType = "MIN";
        String dateFrom = LocalDate.of(2018, 1, 1).format(dateFormatter);
        String dateTo = LocalDate.of(2019, 6, 1).format(dateFormatter);

        setupInfoServiceSchemaMock(caseType, infoSchemaMIN);

        HttpEntity httpEntity = new HttpEntity(headers);
        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + String.format("/export?fromDate=%s&toDate=%s&caseType=%s&exportType=%s", dateFrom, dateTo, caseType, ExportType.CASE_DATA)
                , HttpMethod.GET, httpEntity, String.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);

        //Attempt to parse CSV to assert valid
        StringReader reader = new StringReader(result.getBody());
        CSVParser csvParser = new CSVParser(reader, CSVFormat.EXCEL.withFirstRecordAsHeader().withTrim());

        //check expected CSV row count
        assertThat(csvParser.getRecords().size()).isEqualTo(3);
    }


    @Test
    public void shouldReturn400WhenInvalidFromDateRequested() throws IOException {
        String caseType = "MIN";
        String dateFrom = "2018/1/55";
        String dateTo = LocalDate.of(2019, 6, 1).format(dateFormatter);
        HttpEntity httpEntity = new HttpEntity(headers);
        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + String.format("/export?fromDate=%s&toDate=%s&caseType=%s&exportType=%s", dateFrom, dateTo, caseType, ExportType.CASE_DATA)
                , HttpMethod.GET, httpEntity, String.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void shouldReturn400WhenInvalidToDateRequested() throws IOException {
        String caseType = "MIN";
        String dateFrom = LocalDate.of(2019, 6, 1).format(dateFormatter);
        String dateTo = "2018/1/55";
        HttpEntity httpEntity = new HttpEntity(headers);
        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + String.format("/export?fromDate=%s&toDate=%s&caseType=%s&exportType=%s", dateFrom, dateTo, caseType, ExportType.CASE_DATA)
                , HttpMethod.GET, httpEntity, String.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }


    @Test
    public void shouldReturn400WhenInvalidExportTypeRequested() throws IOException {
        String caseType = "MIN";
        String dateFrom = LocalDate.of(2018, 1, 1).format(dateFormatter);
        String dateTo = LocalDate.of(2019, 6, 1).format(dateFormatter);
        HttpEntity httpEntity = new HttpEntity(headers);
        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + String.format("/export?fromDate=%s&toDate=%s&caseType=%s&exportType=%s", dateFrom, dateTo, caseType, "BAD_TYPE")
                , HttpMethod.GET, httpEntity, String.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void shouldReturn500WhenUnableToRetrieveDataFromInfoService() {
        String caseType = "MIN";
        String dateFrom = LocalDate.of(2018, 1, 1).format(dateFormatter);
        String dateTo = LocalDate.of(2019, 6, 1).format(dateFormatter);
        HttpEntity httpEntity = new HttpEntity(headers);
        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + String.format("/export?fromDate=%s&toDate=%s&caseType=%s&exportType=%s", dateFrom, dateTo, caseType, ExportType.CASE_DATA)
                , HttpMethod.GET, httpEntity, String.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }


    private void setupInfoServiceSchemaMock(String caseType, Resource result) {
        mockInfoService
                .expect(requestTo(String.format("http://localhost:8085/schema/caseType/%s/reporting", caseType)))
                .andExpect(method(GET))
                .andRespond(withSuccess(result, MediaType.APPLICATION_JSON_UTF8));
    }

    private void setupInfoServiceCaseTypesMock() {
        mockInfoService
                .expect(requestTo("http://localhost:8085/caseType"))
                .andExpect(method(GET))
                .andRespond(withSuccess(infoCaseTypes, MediaType.APPLICATION_JSON_UTF8));
    }

    private MockRestServiceServer buildMockService(RestTemplate restTemplate) {
        MockRestServiceServer.MockRestServiceServerBuilder infoBuilder = bindTo(restTemplate);
        infoBuilder.ignoreExpectOrder(true);
        return infoBuilder.build();
    }

    private String getBasePath() {
        return "http://localhost:" + port;
    }
}
