package uk.gov.digital.ho.hocs.audit.export;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import uk.gov.digital.ho.hocs.audit.application.SpringConfiguration;
import uk.gov.digital.ho.hocs.audit.application.ZonedDateTimeConverter;
import uk.gov.digital.ho.hocs.audit.auditdetails.model.AuditData;
import uk.gov.digital.ho.hocs.audit.auditdetails.repository.AuditRepository;
import uk.gov.digital.ho.hocs.audit.export.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.audit.export.infoclient.dto.*;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AuditExportServiceTest {

    @Mock
    private AuditRepository auditRepository;

    @Mock
    private InfoClient infoClient;

    @Mock
    private ExportDataConverter exportDataConverter;

    @Mock
    private HeaderConverter passThroughHeaderConverter;

    @Mock
    private HeaderConverter headerConverter;

    @Mock
    private HeaderConverter caseNoteHeaderConverter;

    private ExportService exportService;
    private ExportService exportServiceTestHeaders;
    private ExportService exportServiceCaseNotesHeaders;
    private SpringConfiguration configuration = new SpringConfiguration();
    private ObjectMapper mapper;
    private Set<CaseTypeDto> caseTypes = new HashSet<CaseTypeDto>() {{
        add(new CaseTypeDto("DCU Ministerial", "a1", "MIN"));
    }};
    private LocalDateTime from = LocalDateTime.of(2019, 1, 1, 0, 0);
    private LocalDateTime to = LocalDateTime.of(LocalDate.of(2019, 6, 1), LocalTime.MAX);
    private String caseType = "MIN";
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final ZonedDateTimeConverter defaultZonedDateTimeConverter = new ZonedDateTimeConverter(null, null);

    private LinkedHashSet<String> fields = Stream.of(
            "CopyNumberTen",
            "DateOfCorrespondence",
            "OGDDept",
            "InitialDraftDecision",
            "PrivateOfficeDecision",
            "Topics",
            "MarkupDecision",
            "NoReplyNeededConfirmation",
            "OverridePOTeamUUID",
            "QAResponseDecision",
            "DateReceived",
            "OfflineQA",
            "OverrideDraftingTeamUUID",
            "OriginalChannel",
            "ResponseChannel",
            "OfflineQaUser",
            "DraftingTeamName",
            "POTeamName",
            "Correspondents",
            "TransferConfirmation",
            "MinisterSignOffDecision",
            "PrivateOfficeOverridePOTeamUUID",
            "DispatchDecision").collect(Collectors.toCollection(LinkedHashSet::new));

    @Before
    public void setup() {
        mapper = configuration.initialiseObjectMapper();
        when(infoClient.getCaseTypes()).thenReturn(caseTypes);
        List<String> headerList = Stream.of("ID", "User", "Name", "Surname", "Email Address").collect(Collectors.toList());
        List<String> caseNotesHeaderList = Stream.of("timestamp", "event", "userId", "caseUuid", "uuid", "convertedCaseNoteType", "text").collect(Collectors.toList());
        when(headerConverter.substitute(anyList())).thenReturn(headerList);
        when(caseNoteHeaderConverter.substitute(anyList())).thenReturn(caseNotesHeaderList);
        when(passThroughHeaderConverter.substitute(anyList())).thenAnswer(new Answer<List<String>>() {
            @Override
            public List<String> answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                return (List<String>) args[0];
            }
        });
        exportService = new ExportService(auditRepository, mapper, infoClient, exportDataConverter, passThroughHeaderConverter);
        exportServiceTestHeaders = new ExportService(auditRepository, mapper, infoClient, exportDataConverter, headerConverter);
        exportServiceCaseNotesHeaders = new ExportService(auditRepository, mapper, infoClient, exportDataConverter, caseNoteHeaderConverter);
    }

    @Test
    public void caseDataExportShouldReturnCSVData() throws IOException {

        when(infoClient.getCaseExportFields("MIN")).thenReturn(fields);

        when(auditRepository.findLastAuditDataByDateRangeAndEvents(any(), any(), eq(ExportService.CASE_DATA_EVENTS), any())).thenReturn(getCaseDataAuditData().stream());

        OutputStream outputStream = new ByteArrayOutputStream();
        exportService.auditExport(from.toLocalDate(), to.toLocalDate(), outputStream, caseType, ExportType.CASE_DATA, false, false, null, null);

        List<CSVRecord> rows = getCSVRows(outputStream.toString());
        assertThat(rows.size()).isEqualTo(3);

        CSVRecord row = rows.get(0);
        assertThat(row.get("CopyNumberTen")).isEqualTo("FALSE");
        assertThat(row.get("DateReceived")).isEqualTo("2019-04-23");
        assertThat(row.get("Correspondents")).isEqualTo("09a89901-d2f1-4778-befe-ebab57659b90");
        assertThat(row.get("OriginalChannel")).isEqualTo("EMAIL");
        assertThat(row.get("DateOfCorrespondence")).isEqualTo("2019-04-23");
        assertThat(row.get("caseType")).isEqualTo("MIN");
        assertThat(row.get("caseUuid")).isEqualTo("3e5cf44f-e86a-4b21-891a-018e2343cda1");
        assertThat(row.get("reference")).isEqualTo("MIN/0120101/19");
        assertThat(row.get("deadline")).isEqualTo("2019-05-22");
        assertThat(row.get("DateReceived")).isEqualTo("2019-04-23");
        assertThat(row.get("primaryTopic")).isEmpty();
        assertThat(row.get("primaryCorrespondent")).isEqualTo("09a89901-d2f1-4778-befe-ebab57659b90");
    }

    @Test
    public void caseDataExportShouldReturnRowHeaders() throws IOException {
        Set<String> expectedHeaders = Stream.of("timestamp", "event", "userId", "caseUuid", "reference", "caseType", "deadline", "primaryCorrespondent", "primaryTopic").collect(Collectors.toSet());
        expectedHeaders.addAll(fields);

        when(infoClient.getCaseExportFields("MIN")).thenReturn(fields);
        when(auditRepository.findLastAuditDataByDateRangeAndEvents(any(), any(), any(), any())).thenReturn(getCaseDataAuditData().stream());

        OutputStream outputStream = new ByteArrayOutputStream();
        exportService.auditExport(from.toLocalDate(), to.toLocalDate(), outputStream, caseType, ExportType.CASE_DATA, false, false, null, null);

        String csvBody = outputStream.toString();
        Set<String> headers = getCSVHeaders(csvBody).keySet();
        assertThat(headers).containsExactlyInAnyOrder(expectedHeaders.toArray(new String[expectedHeaders.size()]));
    }

    @Test
    public void caseDataExportShouldOnlyRequestCreateUpdateEventsAndCaseType() throws IOException {

        when(infoClient.getCaseExportFields("MIN")).thenReturn(fields);
        when(auditRepository.findLastAuditDataByDateRangeAndEvents(any(), any(), eq(ExportService.CASE_DATA_EVENTS), any())).thenReturn(getCaseDataAuditData().stream());

        OutputStream outputStream = new ByteArrayOutputStream();
        exportService.auditExport(from.toLocalDate(), to.toLocalDate(), outputStream, caseType, ExportType.CASE_DATA, false, false, null, null);

        verify(auditRepository, times(1)).findLastAuditDataByDateRangeAndEvents(from, to, ExportService.CASE_DATA_EVENTS, "a1");
        verify(exportDataConverter, times(0)).convertData(any());
    }

    @Test
    public void caseDataExportWhenConvertThenConvertDataInvokedOncePerCaseAuditData() throws IOException {

        when(infoClient.getCaseExportFields("MIN")).thenReturn(fields);
        when(auditRepository.findLastAuditDataByDateRangeAndEvents(any(), any(), eq(ExportService.CASE_DATA_EVENTS), any())).thenReturn(getCaseDataAuditData().stream());
        when(exportDataConverter.convertData(any())).thenAnswer(a -> a.getArguments()[0]);

        OutputStream outputStream = new ByteArrayOutputStream();
        exportService.auditExport(from.toLocalDate(), to.toLocalDate(), outputStream, caseType, ExportType.CASE_DATA, true, false, null, null);

        verify(auditRepository, times(1)).findLastAuditDataByDateRangeAndEvents(from, to, ExportService.CASE_DATA_EVENTS, "a1");
        verify(exportDataConverter, times(getCaseDataAuditData().size())).convertData(any());
    }

    @Test
    public void caseNotesExportShouldReturnCsvData() throws IOException {

        when(auditRepository.findAuditDataByDateRangeAndEvents(any(), any(), eq(ExportService.CASE_NOTES_EVENTS), any())).thenReturn(getCaseNotesAuditData().stream());
        Set<String> expectedHeaders = Stream.of("timestamp", "event", "userId", "caseUuid", "uuid", "caseNoteType", "text").collect(Collectors.toSet());
        OutputStream outputStream = new ByteArrayOutputStream();

        exportService.auditExport(from.toLocalDate(), to.toLocalDate(), outputStream, "MIN", ExportType.CASE_NOTES, false, false, null, null);

        String csvBody = outputStream.toString();
        Set<String> headers = getCSVHeaders(csvBody).keySet();
        assertThat(headers).containsExactlyInAnyOrder(expectedHeaders.toArray(new String[expectedHeaders.size()]));

        List<CSVRecord> rows = getCSVRows(outputStream.toString());
        assertThat(rows.size()).isEqualTo(2);

        CSVRecord row = rows.get(0);
        assertThat(row.get("event")).isEqualTo("CASE_NOTE_CREATED");
        assertThat(row.get("caseNoteType")).isEqualTo("Type1");
        assertThat(row.get("text")).isEqualTo("Note 1");
        verify(exportDataConverter, times(0)).convertData(any());
    }

    @Test
    public void caseNotesExportShouldConvertHeadersAndData() throws IOException {

        when(auditRepository.findAuditDataByDateRangeAndEvents(any(), any(), eq(ExportService.CASE_NOTES_EVENTS), any())).thenReturn(getCaseNotesAuditData().stream());
        when(exportDataConverter.convertData(any())).thenAnswer(a -> a.getArguments()[0]);
        Set<String> expectedHeaders = Stream.of("timestamp", "event", "userId", "caseUuid", "uuid", "convertedCaseNoteType", "text").collect(Collectors.toSet());
        OutputStream outputStream = new ByteArrayOutputStream();

        exportServiceCaseNotesHeaders.auditExport(from.toLocalDate(), to.toLocalDate(), outputStream, "MIN", ExportType.CASE_NOTES, true, true, null, null);

        String csvBody = outputStream.toString();
        Set<String> headers = getCSVHeaders(csvBody).keySet();
        assertThat(headers).containsExactlyInAnyOrder(expectedHeaders.toArray(new String[expectedHeaders.size()]));

        List<CSVRecord> rows = getCSVRows(outputStream.toString());
        assertThat(rows.size()).isEqualTo(2);

        CSVRecord row = rows.get(0);
        assertThat(row.get("event")).isEqualTo("CASE_NOTE_CREATED");
        assertThat(row.get("convertedCaseNoteType")).isEqualTo("Type1");
        assertThat(row.get("text")).isEqualTo("Note 1");
        verify(exportDataConverter, times(2)).convertData(any());
    }

    @Test
    public void auditSomuExportShouldReturnCSVData() throws IOException {
        SomuTypeDto somuTypeDto = new SomuTypeDto(UUID.fromString("655ddfa7-5ccf-4d9b-86fd-8cef5f61a318"), "MIN", "somuType", "{\"fields\":[{\"name\":\"field1\"},{\"name\":\"field2\"}]}", true);
        when(infoClient.getSomuType("MIN", "somuType")).thenReturn(somuTypeDto);
        when(auditRepository.findLastAuditDataByDateRangeAndEvents(any(), any(), eq(ExportService.SOMU_TYPE_EVENTS), any())).thenReturn(getCaseDataWithSomuTypeAuditData().stream());

        OutputStream outputStream = new ByteArrayOutputStream();
        exportService.auditSomuExport(from.toLocalDate(), to.toLocalDate(), outputStream, caseType, "somuType", false, null, null);

        List<CSVRecord> rows = getCSVRows(outputStream.toString());
        assertThat(rows.size()).isEqualTo(1);

        CSVRecord row = rows.get(0);
        assertThat(row.get("field1")).isEqualTo("value1");
        assertThat(row.get("field2")).isEqualTo("value2");
    }

    @Test
    public void caseTopicExportShouldReturnRowHeaders() throws IOException {
        String[] expectedHeaders = new String[]{"timestamp", "event" ,"userId", "caseUuid", "topicUuid", "topic"};

        when(auditRepository.findAuditDataByDateRangeAndEvents(any(), any(), any(), any())).thenReturn(getTopicDataAuditData().stream());

        OutputStream outputStream = new ByteArrayOutputStream();
        exportService.auditExport(from.toLocalDate(), to.toLocalDate(), outputStream, caseType, ExportType.TOPICS, false, false, null, null);

        String csvBody = outputStream.toString();
        Set<String> headers = getCSVHeaders(csvBody).keySet();
        assertThat(headers).containsExactlyInAnyOrder(expectedHeaders);
    }

    @Test
    public void caseTopicExportShouldOnlyRequestTopicEventsAndCaseType() throws IOException {
        OutputStream outputStream = new ByteArrayOutputStream();
        when(auditRepository.findAuditDataByDateRangeAndEvents(any(), any(), any(), any())).thenReturn(getTopicDataAuditData().stream());
        exportService.auditExport(from.toLocalDate(), to.toLocalDate(), outputStream, caseType, ExportType.TOPICS, false, false, null, null);
        verify(auditRepository, times(1)).findAuditDataByDateRangeAndEvents(from, to, ExportService.TOPIC_EVENTS, "a1");
        verify(exportDataConverter, times(0)).convertData(any());
    }

    @Test
    public void caseCorrespondentsExportShouldReturnRowHeaders() throws IOException {
        String[] expectedHeaders = new String[]{"timestamp", "event" ,"userId","caseUuid",
                "correspondentUuid", "fullname", "address1", "address2",
                "address3", "country", "postcode", "telephone", "email",
                "reference", "externalKey"};

        when(auditRepository.findAuditDataByDateRangeAndEvents(any(), any(), any(), any())).thenReturn(getCorrespondentDataAuditData().stream());

        OutputStream outputStream = new ByteArrayOutputStream();
        exportService.auditExport(from.toLocalDate(), to.toLocalDate(), outputStream, caseType, ExportType.CORRESPONDENTS, false, false, null, null);

        String csvBody = outputStream.toString();
        Set<String> headers = getCSVHeaders(csvBody).keySet();
        assertThat(headers).containsExactlyInAnyOrder(expectedHeaders);
    }

    @Test
    public void caseCorrespondentExportShouldOnlyRequestCorrespondentEventsAndCaseType() throws IOException {
        when(auditRepository.findAuditDataByDateRangeAndEvents(any(), any(), any(), any())).thenReturn(getCorrespondentDataAuditData().stream());
        OutputStream outputStream = new ByteArrayOutputStream();
        exportService.auditExport(from.toLocalDate(), to.toLocalDate(), outputStream, caseType, ExportType.CORRESPONDENTS, false, false, null, null);
        verify(auditRepository, times(1)).findAuditDataByDateRangeAndEvents(from, to, ExportService.CORRESPONDENT_EVENTS, "a1");
        verify(exportDataConverter, times(0)).convertData(any());
    }

    @Test
    public void caseAllocationsExportShouldReturnRowHeaders() throws IOException {
        String[] expectedHeaders = new String[]{"timestamp", "event" ,"userId","caseUuid","stage", "allocatedTo", "deadline"};
        when(auditRepository.findAuditDataByDateRangeAndEvents(any(), any(), any(), any())).thenReturn(getAllocationDataAuditData().stream());
        OutputStream outputStream = new ByteArrayOutputStream();
        exportService.auditExport(from.toLocalDate(), to.toLocalDate(), outputStream, caseType, ExportType.ALLOCATIONS, false, false, null, null);

        String csvBody = outputStream.toString();
        Set<String> headers = getCSVHeaders(csvBody).keySet();
        assertThat(headers).containsExactlyInAnyOrder(expectedHeaders);
    }

    @Test
    public void caseAllocationsExportShouldOnlyRequestCreateUpdateEventsAndCaseType() throws IOException {
        when(auditRepository.findAuditDataByDateRangeAndEvents(any(), any(), any(), any())).thenReturn(getAllocationDataAuditData().stream());
        OutputStream outputStream = new ByteArrayOutputStream();
        exportService.auditExport(from.toLocalDate(), to.toLocalDate(), outputStream, caseType, ExportType.ALLOCATIONS, false, false, null, null);
        verify(auditRepository, times(1)).findAuditDataByDateRangeAndEvents(from, to, ExportService.ALLOCATION_EVENTS, "a1");
        verify(exportDataConverter, times(0)).convertData(any());
    }


    @Test
    public void staticTopicExportShouldReturnCSV() throws IOException {
        String[] expectedHeaders = new String[]{"topicUUID", "topicName"};

        LinkedHashSet<TopicDto> topics = new LinkedHashSet<TopicDto>(){{
            add(new TopicDto("Topic 1", UUID.randomUUID()));
            add(new TopicDto("Topic 2", UUID.randomUUID()));
        }};

        when(infoClient.getTopics()).thenReturn(topics);

        OutputStream outputStream = new ByteArrayOutputStream();
        exportService.staticTopicExport(outputStream, false);

        String csvBody = outputStream.toString();
        Set<String> headers = getCSVHeaders(csvBody).keySet();
        List<CSVRecord> rows = getCSVRows(csvBody);
        assertThat(rows.size()).isEqualTo(2);
        assertThat(headers).containsExactlyInAnyOrder(expectedHeaders);
        assertThat(rows.get(0).get("topicName")).isEqualTo("Topic 1");
    }

    @Test
    public void staticTeamExportShouldReturnCSV() throws IOException {
        String[] expectedHeaders = new String[]{"teamUUID", "teamName"};

        LinkedHashSet<TeamDto> teams = new LinkedHashSet<TeamDto>(){{
            add(new TeamDto("Team 1", UUID.randomUUID(), true, UUID.randomUUID().toString()));
            add(new TeamDto("Team 2", UUID.randomUUID(), true, UUID.randomUUID().toString()));
        }};

        when(infoClient.getTeams()).thenReturn(teams);

        OutputStream outputStream = new ByteArrayOutputStream();
        exportService.staticTeamExport(outputStream, false);

        String csvBody = outputStream.toString();
        Set<String> headers = getCSVHeaders(csvBody).keySet();
        List<CSVRecord> rows = getCSVRows(csvBody);
        assertThat(rows.size()).isEqualTo(2);
        assertThat(headers).containsExactlyInAnyOrder(expectedHeaders);
        assertThat(rows.get(0).get("teamName")).isEqualTo("Team 1");
    }

    @Test
    public void staticUnitsForTeamsExportShouldReturnCSV() throws IOException {
        String[] expectedHeaders = new String[]{"unitUUID", "unitName", "teamUUID", "teamName"};
        String unitUUID = UUID.randomUUID().toString();
        LinkedHashSet<UnitDto> units = new LinkedHashSet<UnitDto>(){{
           add(new UnitDto("Unit 1", unitUUID, "1"));
        }};
        LinkedHashSet<TeamDto> teams = new LinkedHashSet<TeamDto>(){{
            add(new TeamDto("Team 1", UUID.randomUUID(), true, UUID.randomUUID().toString()));
            add(new TeamDto("Team 2", UUID.randomUUID(), true, UUID.randomUUID().toString()));
        }};
        when(infoClient.getUnits()).thenReturn(units);
        when(infoClient.getTeamsForUnit(unitUUID)).thenReturn(teams);

        OutputStream outputStream = new ByteArrayOutputStream();
        exportService.staticUnitsForTeamsExport(outputStream, false);

        String csvBody = outputStream.toString();
        Set<String> headers = getCSVHeaders(csvBody).keySet();
        List<CSVRecord> rows = getCSVRows(csvBody);
        assertThat(rows.size()).isEqualTo(2);
        assertThat(headers).containsExactlyInAnyOrder(expectedHeaders);
        assertThat(rows.get(0).get("unitName")).isEqualTo("Unit 1");
        assertThat(rows.get(0).get("teamName")).isEqualTo("Team 1");
        assertThat(rows.get(1).get("unitName")).isEqualTo("Unit 1");
        assertThat(rows.get(1).get("teamName")).isEqualTo("Team 2");
    }

    @Test
    public void staticUserExportShouldReturnCSV() throws IOException {
        String[] expectedHeaders = new String[]{"userUUID", "username", "firstName", "lastName", "email"};

        LinkedHashSet<UserDto> users = new LinkedHashSet<UserDto>(){{
            add(new UserDto(UUID.randomUUID().toString(), "User 1","first name", "last name", "email address"));
            add(new UserDto(UUID.randomUUID().toString(), "User 2","first name", "last name", "email address"));
        }};

        when(infoClient.getUsers()).thenReturn(users);

        OutputStream outputStream = new ByteArrayOutputStream();
        exportService.staticUserExport(outputStream, false);

        String csvBody = outputStream.toString();
        Set<String> headers = getCSVHeaders(csvBody).keySet();
        List<CSVRecord> rows = getCSVRows(csvBody);
        assertThat(rows.size()).isEqualTo(2);
        assertThat(headers).containsExactlyInAnyOrder(expectedHeaders);
        assertThat(rows.get(0).get("username")).isEqualTo("User 1");
    }

    @Test
    public void staticUserExportShouldReturnCSVWithSubstitutedHeadersOrNot() throws IOException {
        String[] expectedHeaders = new String[]{"userUUID", "username", "firstName", "lastName", "email"};
        String[] expectedSubstitutedHeaders = new String[]{"ID", "User", "Name", "Surname", "Email Address"};

        OutputStream substitutedOutputStream = new ByteArrayOutputStream();
        exportServiceTestHeaders.staticUserExport(substitutedOutputStream, true);

        String csvBody = substitutedOutputStream.toString();
        Set<String> headers = getCSVHeaders(csvBody).keySet();
        assertThat(headers).containsExactlyInAnyOrder(expectedSubstitutedHeaders);

        OutputStream outputStream = new ByteArrayOutputStream();
        exportServiceTestHeaders.staticUserExport(outputStream, false);

        csvBody = outputStream.toString();
        headers = getCSVHeaders(csvBody).keySet();
        assertThat(headers).containsExactlyInAnyOrder(expectedHeaders);
    }

    @Test
    public void verifyHeadersAreSubstitutedWithCaseDataExtract() throws IOException {
        OutputStream outputStream = new ByteArrayOutputStream();
        OutputStream buffer = new BufferedOutputStream(outputStream);
        OutputStreamWriter outputWriter = new OutputStreamWriter(buffer, "UTF-8");
        exportService.caseDataExport(LocalDate.MIN, LocalDate.MAX, outputWriter, "a1", "MIN", true, true, defaultZonedDateTimeConverter);
        verify(passThroughHeaderConverter, times(1)).substitute(anyList());
    }

    @Test
    public void verifyHeadersAreSubstitutedWithAllocationsExtract() throws IOException {
        OutputStream outputStream = new ByteArrayOutputStream();
        OutputStream buffer = new BufferedOutputStream(outputStream);
        OutputStreamWriter outputWriter = new OutputStreamWriter(buffer, "UTF-8");
        exportService.allocationExport(LocalDate.MIN, LocalDate.MAX, outputWriter, "a1", true, true, defaultZonedDateTimeConverter);
        verify(passThroughHeaderConverter, times(1)).substitute(anyList());
    }

    @Test
    public void verifyHeadersAreSubstitutedWithCorrespondentExtract() throws IOException {
        OutputStream outputStream = new ByteArrayOutputStream();
        OutputStream buffer = new BufferedOutputStream(outputStream);
        OutputStreamWriter outputWriter = new OutputStreamWriter(buffer, "UTF-8");
        exportService.correspondentExport(LocalDate.MIN, LocalDate.MAX, outputWriter, "a1", true, true, defaultZonedDateTimeConverter);
        verify(passThroughHeaderConverter, times(1)).substitute(anyList());
    }

    @Test
    public void verifyHeadersAreSubstitutedWithTopicExtract() throws IOException {
        OutputStream outputStream = new ByteArrayOutputStream();
        OutputStream buffer = new BufferedOutputStream(outputStream);
        OutputStreamWriter outputWriter = new OutputStreamWriter(buffer, "UTF-8");
        exportService.topicExport(LocalDate.MIN, LocalDate.MAX, outputWriter, "a1", true, defaultZonedDateTimeConverter);
        verify(passThroughHeaderConverter, times(1)).substitute(anyList());
    }

    @Test
    public void verifyHeadersAreSubstitutedWithStaticTeamExtract() throws IOException {
        OutputStream outputStream = new ByteArrayOutputStream();
        exportService.staticTeamExport(outputStream, true);
        verify(passThroughHeaderConverter, times(1)).substitute(anyList());
    }

    @Test
    public void verifyHeadersAreSubstitutedWithStaticTopicExtract() throws IOException {
        OutputStream outputStream = new ByteArrayOutputStream();
        exportService.staticTopicExport(outputStream, true);
        verify(passThroughHeaderConverter, times(1)).substitute(anyList());
    }

    @Test
    public void verifyHeadersAreSubstitutedWithStaticTopicWithTeamExtract() throws IOException {
        OutputStream outputStream = new ByteArrayOutputStream();
        exportService.staticTopicsWithTeamsExport(outputStream, "MIN", true);
        verify(passThroughHeaderConverter, times(1)).substitute(anyList());
    }

    @Test
    public void verifyHeadersAreSubstitutedWithStaticUnitsForTeamsExtract() throws IOException {
        OutputStream outputStream = new ByteArrayOutputStream();
        exportService.staticUnitsForTeamsExport(outputStream, true);
        verify(passThroughHeaderConverter, times(1)).substitute(anyList());
    }

    @Test
    public void verifyHeadersAreSubstitutedWithStaticUserExtract() throws IOException {
        OutputStream outputStream = new ByteArrayOutputStream();
        exportService.staticUserExport(outputStream, true);
        verify(passThroughHeaderConverter, times(1)).substitute(anyList());
    }

    private List<CSVRecord> getCSVRows(String csvBody) throws IOException {
        StringReader reader = new StringReader(csvBody);
        CSVParser csvParser = new CSVParser(reader, CSVFormat.EXCEL.withFirstRecordAsHeader().withTrim());
        return csvParser.getRecords();
    }

    private Map<String, Integer> getCSVHeaders(String csvBody) throws IOException {
        StringReader reader = new StringReader(csvBody);
        CSVParser csvParser = new CSVParser(reader, CSVFormat.EXCEL.withFirstRecordAsHeader().withTrim());
        return csvParser.getHeaderMap();
    }

    private LinkedHashSet<AuditData> getCaseDataAuditData() {
         return new LinkedHashSet<AuditData>(){{
             add(new AuditData(UUID.fromString("3e5cf44f-e86a-4b21-891a-018e2343cda1"),UUID.randomUUID(),UUID.randomUUID().toString(),"a-service", "{\"data\": {\"valid\": \"true\", \"DateReceived\": \"2019-04-23\", \"CopyNumberTen\": \"FALSE\", \"Correspondents\": \"09a89901-d2f1-4778-befe-ebab57659b90\", \"OriginalChannel\": \"EMAIL\", \"DateOfCorrespondence\": \"2019-04-23\"}, \"type\": \"MIN\", \"uuid\": \"3e5cf44f-e86a-4b21-891a-018e2343cda1\", \"created\": \"2019-04-23T12:57:19.738532\", \"reference\": \"MIN/0120101/19\", \"caseDeadline\": \"2019-05-22\", \"dateReceived\": \"2019-04-23\", \"primaryTopic\": null, \"primaryCorrespondent\": \"09a89901-d2f1-4778-befe-ebab57659b90\"}", "an-env", LocalDateTime.parse("2019-04-23 12:58:04",dateFormatter), "CASE_UPDATED", UUID.randomUUID().toString()));
             add(new AuditData(UUID.fromString("3e5cf44f-e86a-4b21-891a-018e2343cda1"),UUID.randomUUID(),UUID.randomUUID().toString(),"a-service", "{\"type\": \"MIN\", \"uuid\": \"3e5cf44f-e86a-4b21-891a-018e2343cda1\", \"created\": \"2019-04-23T09:18:26.446343\", \"reference\": \"MIN/0120091/19\", \"caseDeadline\": \"2019-05-22\", \"dateReceived\": \"2019-04-23\"}", "an-env", LocalDateTime.parse("2019-04-23 09:18:26", dateFormatter), "CASE_CREATED", UUID.randomUUID().toString()));
             add(new AuditData(UUID.fromString("a7590ff3-4377-4ee8-a165-0c6426c744a1"),UUID.randomUUID(),UUID.randomUUID().toString(),"a-service", "{\"type\": \"MIN\", \"uuid\": \"a7590ff3-4377-4ee8-a165-0c6426c744a1\", \"created\": \"2019-04-23T11:17:53.155776\", \"reference\": \"MIN/0120092/19\", \"caseDeadline\": \"2019-05-22\", \"dateReceived\": \"2019-04-23\"}", "an-env", LocalDateTime.parse("2019-04-23 11:17:53", dateFormatter), "CASE_CREATED", UUID.randomUUID().toString()));

        }};
    }

    private LinkedHashSet<AuditData> getCaseNotesAuditData() {
         return new LinkedHashSet<AuditData>(){{
             add(new AuditData(UUID.fromString("3e5cf44f-e86a-4b21-891a-018e2343cda1"),UUID.randomUUID(),UUID.randomUUID().toString(),"a-service", "{\"caseNoteType\": \"Type1\", \"text\": \"Note 1\" }", "an-env", LocalDateTime.parse("2019-04-23 09:18:26", dateFormatter), "CASE_NOTE_CREATED", UUID.randomUUID().toString()));
             add(new AuditData(UUID.fromString("a7590ff3-4377-4ee8-a165-0c6426c744a1"),UUID.randomUUID(),UUID.randomUUID().toString(),"a-service", "{\"caseNoteType\": \"Type2\", \"text\": \"Note 2\" }", "an-env", LocalDateTime.parse("2019-04-23 11:17:53", dateFormatter), "CASE_NOTE_CREATED", UUID.randomUUID().toString()));
        }};
    }

    private Set<AuditData> getCaseDataWithSomuTypeAuditData() {
        return new HashSet<AuditData>(){{
            add(new AuditData(UUID.fromString("3e5cf44f-e86a-4b21-891a-018e2343cda1"),UUID.randomUUID(),UUID.randomUUID().toString(),"a-service", "{\"somuItems\":[{\"uuid\":\"09a89901-d2f1-4778-befe-ebab57659b90\",\"somuTypeUUID\":\"655ddfa7-5ccf-4d9b-86fd-8cef5f61a318\",\"data\":{\"field1\":\"value1\",\"field2\":\"value2\"}}]}", "an-env", LocalDateTime.parse("2019-04-23 12:48:33",dateFormatter), "CASE_TOPIC_CREATED", UUID.randomUUID().toString()));
        }};
    }

    private Set<AuditData> getTopicDataAuditData() {
        return new HashSet<AuditData>(){{
            add(new AuditData(UUID.fromString("3e5cf44f-e86a-4b21-891a-018e2343cda1"),UUID.randomUUID(),UUID.randomUUID().toString(),"a-service", "{\"topicName\": \"Cardiff University Kittens\", \"topicUuid\": \"56926a98-de02-49c6-8457-4e6782ac7d6e\"}", "an-env", LocalDateTime.parse("2019-04-23 12:48:33",dateFormatter), "CASE_TOPIC_CREATED", UUID.randomUUID().toString()));
        }};
    }

    private Set<AuditData> getCorrespondentDataAuditData() {
        return new HashSet<AuditData>(){{
            add(new AuditData(UUID.fromString("3e5cf44f-e86a-4b21-891a-018e2343cda1"),UUID.randomUUID(),UUID.randomUUID().toString(),"a-service", "{\"type\": \"MEMBER\", \"uuid\": \"09a89901-d2f1-4778-befe-ebab57659b90\", \"email\": null, \"address\": {\"country\": \"United Kingdom\", \"address1\": \"House of Commons\", \"address2\": \"London\", \"address3\": null, \"postcode\": \"SW1A 0AA\"}, \"created\": \"2019-04-23T12:57:58.823287\", \"caseUUID\": \"3e5cf44f-e86a-4b21-891a-018e2343cda1\", \"fullname\": \"Christina Rees MP\", \"reference\": null, \"telephone\": null}", "an-env", LocalDateTime.parse("2019-04-23 12:57:58",dateFormatter), "CORRESPONDENT_CREATED", UUID.randomUUID().toString()));
        }};
    }

    private Set<AuditData> getAllocationDataAuditData() {
        return new HashSet<AuditData>(){{
            add(new AuditData(UUID.fromString("3e5cf44f-e86a-4b21-891a-018e2343cda1"),UUID.randomUUID(),UUID.randomUUID().toString(),"a-service", "{\"stage\": \"DCU_MIN_DATA_INPUT\", \"teamUUID\": \"1102b26b-06ed-4247-a1b3-699167f2dbcd\", \"stageUUID\": \"808be858-1a4d-4117-99c8-59cf6f90edb3\"}", "an-env", LocalDateTime.parse("2019-04-23 12:58:04",dateFormatter), "STAGE_ALLOCATED_TO_TEAM", UUID.randomUUID().toString()));
            add(new AuditData(UUID.fromString("3e5cf44f-e86a-4b21-891a-018e2343cda1"),UUID.randomUUID(),UUID.randomUUID().toString(),"a-service", "{\"stage\": \"DCU_MIN_DATA_INPUT\", \"teamUUID\": \"1102b26b-06ed-4247-a1b3-699167f2dbcd\", \"stageUUID\": \"808be858-1a4d-4117-99c8-59cf6f90edb3\"}", "an-env", LocalDateTime.parse("2019-04-23 09:18:29",dateFormatter), "STAGE_ALLOCATED_TO_TEAM", UUID.randomUUID().toString()));
            add(new AuditData(UUID.fromString("a7590ff3-4377-4ee8-a165-0c6426c744a1"),UUID.randomUUID(),UUID.randomUUID().toString(),"a-service", "{\"stage\": \"DCU_MIN_DATA_INPUT\", \"teamUUID\": \"1102b26b-06ed-4247-a1b3-699167f2dbcd\", \"stageUUID\": \"64b4c266-7671-4049-882e-82b1269570c2\"}", "an-env", LocalDateTime.parse("2019-04-23 11:17:53", dateFormatter), "STAGE_ALLOCATED_TO_TEAM", UUID.randomUUID().toString()));
        }};
    }

}
