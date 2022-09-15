package uk.gov.digital.ho.hocs.audit.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.digital.ho.hocs.audit.client.info.dto.CaseTypeDto;
import uk.gov.digital.ho.hocs.audit.core.utils.ZonedDateTimeConverter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Set;

import static org.mockito.BDDMockito.given;

public class TopicExportServiceTest extends BaseExportServiceTest {

    @Autowired
    private TopicExportService topicExportService;

    private ZonedDateTimeConverter zonedDateTimeConverter;

    @BeforeEach
    public void setup() {
        zonedDateTimeConverter = new ZonedDateTimeConverter();

        given(infoClient.getCaseTypes()).willReturn(Set.of(new CaseTypeDto("Test", "a1", "TEST")));
    }

    @Test
    public void shouldReturnExport() throws IOException {
        topicExportService.export(LocalDate.of(2020, 1, 1), LocalDate.now().plusDays(1), outputStream, "TEST", false,
            false, zonedDateTimeConverter);

        var result = outputStream.toString(StandardCharsets.UTF_8);
        Assertions.assertNotNull(result);

        var headers = getCsvHeaderRow(result);
        Assertions.assertEquals(6, headers.length);

        var rows = getCsvDataRows(result);
        Assertions.assertEquals(1, rows.size());
    }

}
