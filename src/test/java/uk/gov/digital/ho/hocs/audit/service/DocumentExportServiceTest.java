package uk.gov.digital.ho.hocs.audit.service;

import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.digital.ho.hocs.audit.client.info.dto.CaseTypeDto;
import uk.gov.digital.ho.hocs.audit.core.utils.ZonedDateTimeConverter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.mockito.BDDMockito.given;

public class DocumentExportServiceTest extends BaseExportServiceTest {

    @Autowired
    private DocumentExportService documentExportService;

    private ZonedDateTimeConverter zonedDateTimeConverter;

    @BeforeEach
    public void setup() {
        zonedDateTimeConverter = new ZonedDateTimeConverter();

        given(infoClient.getCaseTypes()).willReturn(Set.of(new CaseTypeDto("Test", "a1", "TEST")));
    }

    @Test
    public void shouldReturnExport() throws IOException {
        documentExportService.export(LocalDate.of(2020, 1, 1), LocalDate.now().plusDays(1), outputStream, "TEST",
            false, false, zonedDateTimeConverter);

        var result = outputStream.toString(StandardCharsets.UTF_8);
        Assertions.assertNotNull(result);

        var headers = getCsvHeaderRow(result);
        Assertions.assertEquals(4, headers.length);

        var rows = getCsvDataRows(result).stream().map(CSVRecord::toList).collect(Collectors.toList());
        var expectedRows = List.of(
            List.of("10000000-0000-0000-0000-000000000001", "2020-01-01T00:00:00.000000", "title", "type")
        );
        Assertions.assertTrue(rows.containsAll(expectedRows));
        Assertions.assertEquals(3, rows.size());
    }

}
