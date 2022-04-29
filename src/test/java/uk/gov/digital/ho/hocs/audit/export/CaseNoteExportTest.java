package uk.gov.digital.ho.hocs.audit.export;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.audit.client.info.InfoClient;
import uk.gov.digital.ho.hocs.audit.client.info.dto.CaseTypeDto;
import uk.gov.digital.ho.hocs.audit.repository.AuditRepository;
import uk.gov.digital.ho.hocs.audit.repository.entity.AuditEvent;
import uk.gov.digital.ho.hocs.audit.service.ExportService;
import uk.gov.digital.ho.hocs.audit.service.domain.ExportType;
import uk.gov.digital.ho.hocs.audit.service.domain.converter.ExportDataConverterFactory;
import uk.gov.digital.ho.hocs.audit.service.domain.converter.HeaderConverter;
import uk.gov.digital.ho.hocs.audit.service.domain.converter.MalformedDateConverter;
import uk.gov.digital.ho.hocs.audit.service.domain.parsers.DataParserFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CaseNoteExportTest {

    private ExportService exportService;

    @Mock
    private AuditRepository auditRepository;

    @Mock
    private InfoClient infoClient;

    @Mock
    private ExportDataConverterFactory exportDataConverterFactory;

    @Mock
    private HeaderConverter passThroughHeaderConverter;

    @Mock
    private MalformedDateConverter malformedDateConverter;

    @Mock
    private DataParserFactory dataParserFactory;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void shouldTrimOverSizedCaseNoteText() throws IOException {
        exportService = createExportService(40000);
        OutputStream outputStream = new ByteArrayOutputStream();
        exportService.auditExport(LocalDate.now(), LocalDate.now(), outputStream, "type", ExportType.CASE_NOTES, false, false, null, null);
        List<CSVRecord> rows = getCSVRows(outputStream.toString());
        assertThat(rows.get(0).get("text").length()).isEqualTo(ExportService.EXCEL_MAX_CELL_SIZE - 1);
    }

    @Test
    public void shouldNotTrimUnderSizedCaseNoteText() throws IOException {
        int caseNoteLength = 250;
        exportService = createExportService(caseNoteLength);
        OutputStream outputStream = new ByteArrayOutputStream();
        exportService.auditExport(LocalDate.now(), LocalDate.now(), outputStream, "type", ExportType.CASE_NOTES, false, false, null, null);
        List<CSVRecord> rows = getCSVRows(outputStream.toString());
        assertThat(rows.get(0).get("text").length()).isEqualTo(caseNoteLength);
    }

    private ExportService createExportService(int lengthOfCaseNote){
        String payload = "{\"text\" : \"" + createCaseNoteText(lengthOfCaseNote) + "\", \"caseNoteType\" : \"SEND_TO_WORKFLOW_MANAGER\"}";
        AuditEvent auditEvent = new AuditEvent("", "", payload, "", LocalDateTime.now(), "", "");
        when(auditRepository.findAuditDataByDateRangeAndEvents(any(), any(), any(), any())).thenReturn(Stream.of(auditEvent));
        when(infoClient.getCaseTypes()).thenReturn(Set.of(new CaseTypeDto("displayName", "a1", "type")));
        when(malformedDateConverter.correctDateFields(any())).then(returnsFirstArg());
        return new ExportService(auditRepository, mapper, infoClient, exportDataConverterFactory, passThroughHeaderConverter, malformedDateConverter, dataParserFactory);
    }

    private String createCaseNoteText(int length){
        return "a".repeat(Math.max(0, length));
    }

    private List<CSVRecord> getCSVRows(String csvBody) throws IOException {
        StringReader reader = new StringReader(csvBody);
        CSVParser csvParser = new CSVParser(reader, CSVFormat.EXCEL.withFirstRecordAsHeader().withTrim());
        return csvParser.getRecords();
    }
}