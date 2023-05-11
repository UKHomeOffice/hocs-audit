package uk.gov.digital.ho.hocs.audit.service;

import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import uk.gov.digital.ho.hocs.audit.client.info.dto.CaseTypeDto;
import uk.gov.digital.ho.hocs.audit.core.utils.ZonedDateTimeConverter;
import uk.gov.digital.ho.hocs.audit.repository.AuditRepository;
import uk.gov.digital.ho.hocs.audit.service.domain.converter.HeaderConverter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

public class CaseDataExportServiceTest extends BaseExportServiceTest {

    @Autowired
    private CaseDataExportService caseDataExportService;

    @Autowired
    private HeaderConverter headerConverter;

    private ZonedDateTimeConverter zonedDateTimeConverter;

    @SpyBean
    private AuditRepository auditRepository;

    @BeforeEach
    public void setup() {
        zonedDateTimeConverter = new ZonedDateTimeConverter();

        given(infoClient.getCaseTypes()).willReturn(Set.of(new CaseTypeDto("Test", "a1", "TEST")));
    }

    @Test
    public void shouldReturnBetweenCaseDataReport() throws IOException {
        LocalDate from = LocalDate.of(2020, 1, 1);
        LocalDate to = LocalDate.now().minusWeeks(1);

        caseDataExportService.export(from, to, outputStream, "TEST", false, false, zonedDateTimeConverter);

        verify(auditRepository).findLastAuditDataByDateRangeAndEvents(LocalDateTime.of(from, LocalTime.MIN),
            LocalDateTime.of(to, LocalTime.MAX), CaseDataExportService.EVENTS, "a1");

        var result = outputStream.toString(StandardCharsets.UTF_8);
        Assertions.assertNotNull(result);

        var headers = getCsvHeaderRow(result);
        Assertions.assertEquals(11, headers.length);

        var rows = getCsvDataRows(result).stream().map(CSVRecord::toList).collect(Collectors.toList());
        var expectedRows = List.of(
            List.of("2020-01-01T00:00:00.000000", "CASE_CREATED", "40000000-0000-0000-0000-000000000000",
                "10000000-0000-0000-0000-000000000000", "TEST", "", "", "", "", "TEST-MIGREF", ""),
            List.of("2020-01-01T01:00:00.000000", "CASE_UPDATED", "40000000-0000-0000-0000-000000000000",
                "10000000-0000-0000-0000-000000000000", "TEST", "", "", "", "", "TEST-MIGREF", "TEST-1"));
        Assertions.assertEquals(3, rows.size());
        Assertions.assertTrue(rows.containsAll(expectedRows));
    }

    @Test
    public void shouldReturnConvertedExport() throws IOException {
        caseDataExportService.export(LocalDate.of(2020, 1, 1), LocalDate.now(), outputStream, "TEST", true, true,
            zonedDateTimeConverter);

        var result = outputStream.toString(StandardCharsets.UTF_8);
        Assertions.assertNotNull(result);

        var headers = getCsvHeaderRow(result);
        Assertions.assertEquals(11, headers.length);
        Assertions.assertArrayEquals(headerConverter.substitute(concatStringArrays(caseDataExportService.getHeaders(),
            caseDataExportService.getAdditionalHeaders(new CaseTypeDto("Test", "a1", "TEST")))), headers);

        var rows = getCsvDataRows(result).stream().map(CSVRecord::toList).collect(Collectors.toList());
        var expectedRows = List.of(
            List.of("2020-01-01T00:00:00.000000", "CASE_CREATED", "40000000-0000-0000-0000-000000000000", "TEST",
                "TEST", "", "", "", "", "TEST-MIGREF", ""),
            List.of(getTodaysLocalDateTime(), "CASE_UPDATED", "40000000-0000-0000-0000-000000000000", "TEST", "TEST",
                "", "", "", "", "TEST-MIGREF", "TEST-1"));
        Assertions.assertEquals(3, rows.size());
        Assertions.assertTrue(rows.containsAll(expectedRows));
    }

    protected String getTodaysLocalDateTime() {
        return zonedDateTimeConverter.convert(LocalDateTime.of(LocalDate.now(), LocalTime.MIN));
    }

    protected String[] concatStringArrays(String[] a, String[] b) {
        return Stream.concat(Arrays.stream(a), Arrays.stream(b)).toArray(String[]::new);
    }

}
