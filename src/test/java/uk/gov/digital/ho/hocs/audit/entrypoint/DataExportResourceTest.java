package uk.gov.digital.ho.hocs.audit.entrypoint;

import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.digital.ho.hocs.audit.client.info.dto.CaseTypeDto;
import uk.gov.digital.ho.hocs.audit.client.info.dto.SomuTypeDto;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.mockito.BDDMockito.given;
import static org.springframework.http.HttpMethod.GET;

@ActiveProfiles({"local", "extracts"})
public class DataExportResourceTest extends BaseExportResourceTest {

    @BeforeEach
    public void setup() {
        given(infoClient.getCaseTypes()).willReturn(Set.of(new CaseTypeDto("Test", "a1", "TEST")));
    }

    @Test
    public void exportTypeExport() throws IOException {
        ResponseEntity<String> result = restTemplate.exchange(
            getExportUri("/export/TEST?fromDate=2020-01-01&toDate=2022-01-01&exportType=CASE_DATA"), GET,
            HttpEntity.EMPTY, String.class);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());
        Assertions.assertEquals(getFileName("test", "case_data"),
            result.getHeaders().getContentDisposition().getFilename());

        var rows = getCSVRows(result.getBody()).stream().map(CSVRecord::toList).collect(Collectors.toList());
        Assertions.assertEquals(1, rows.size());
    }

    @Test
    public void exportTypeReportFailsIfFromDateNotSpecified() {
        ResponseEntity<String> result = restTemplate.exchange(
            getExportUri("/export/TEST?toDate=2022-01-01&exportType=CASE_DATA"), GET, HttpEntity.EMPTY, String.class);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    public void exportTypeReportFailsIfExportTypeNotSpecified() {
        ResponseEntity<String> result = restTemplate.exchange(
            getExportUri("/export/TEST?fromDate=2020-01-01&toDate=2022-01-01"), GET, HttpEntity.EMPTY, String.class);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    public void exportTypeReportFailsIfExportTypeIsInvalid() {
        ResponseEntity<String> result = restTemplate.exchange(
            getExportUri("/export/TEST?fromDate=2020-01-01&toDate=2022-01-01&exportType=TEST"), GET, HttpEntity.EMPTY,
            String.class);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    public void somuTypeExport() throws IOException {
        given(infoClient.getSomuType("TEST", "SOMU")).willReturn(
            new SomuTypeDto(UUID.randomUUID(), "TEST", "SOMU", "{}", true));

        ResponseEntity<String> result = restTemplate.exchange(
            getExportUri("/export/somu/TEST?fromDate=2020-01-01&toDate=2022-01-01&somuType=SOMU"), GET,
            HttpEntity.EMPTY, String.class);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());
        Assertions.assertEquals(getFileName("test", "SOMU"), result.getHeaders().getContentDisposition().getFilename());

        var rows = getCSVRows(result.getBody()).stream().map(CSVRecord::toList).collect(Collectors.toList());
        Assertions.assertEquals(1, rows.size());
    }

    @Test
    public void somuTypeReportFailsIfFromDateNotSpecified() {
        ResponseEntity<String> result = restTemplate.exchange(
            getExportUri("/export/somu/TEST?toDate=2022-01-01&somuType=SOMU"), GET, HttpEntity.EMPTY, String.class);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    public void somuTypeReportFailsIfSomuTypeNotSpecified() {
        ResponseEntity<String> result = restTemplate.exchange(
            getExportUri("/export/somu/TEST?fromDate=2020-01-01&toDate=2022-01-01"), GET, HttpEntity.EMPTY,
            String.class);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

}
