package uk.gov.digital.ho.hocs.audit.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.audit.client.casework.CaseworkClient;
import uk.gov.digital.ho.hocs.audit.client.info.InfoClient;
import uk.gov.digital.ho.hocs.audit.client.info.dto.CaseTypeDto;
import uk.gov.digital.ho.hocs.audit.core.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.audit.core.utils.ZonedDateTimeConverter;
import uk.gov.digital.ho.hocs.audit.repository.AuditRepository;
import uk.gov.digital.ho.hocs.audit.repository.entity.AuditEvent;
import uk.gov.digital.ho.hocs.audit.service.domain.converter.ExportDataConverter;
import uk.gov.digital.ho.hocs.audit.service.domain.converter.HeaderConverter;
import uk.gov.digital.ho.hocs.audit.service.domain.converter.MalformedDateConverter;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Stream;

import static uk.gov.digital.ho.hocs.audit.core.LogEvent.EXPORT_FAILURE_CASE_DATA_DYNAMIC_ROW;

@Slf4j
@Service
public abstract class CaseDataDynamicExportService extends DynamicExportService {

    protected CaseDataDynamicExportService(ObjectMapper objectMapper, AuditRepository auditRepository,
                                        InfoClient infoClient, CaseworkClient caseworkClient,
                                        HeaderConverter headerConverter, MalformedDateConverter malformedDateConverter) {
        super(objectMapper, auditRepository, infoClient, caseworkClient, headerConverter, malformedDateConverter);
    }

    protected void printData(OutputStream outputStream, ZonedDateTimeConverter zonedDateTimeConverter,
                             ExportDataConverter exportDataConverter, boolean convertHeader, CaseTypeDto caseType,
                             Stream<AuditEvent> data) throws IOException {
        var additionalHeaders = getAdditionalHeaders(caseType);
        var concatenatedHeaders = Stream.concat(Arrays.stream(getHeaders()), Arrays.stream(additionalHeaders))
                .toArray(String[]::new);

        if (convertHeader) {
            headerConverter.substitute(concatenatedHeaders);
        }

        printData(outputStream, zonedDateTimeConverter, exportDataConverter, concatenatedHeaders, additionalHeaders, data);
    }

    private void printData(OutputStream outputStream, ZonedDateTimeConverter zonedDateTimeConverter,
                           ExportDataConverter exportDataConverter, String[] headers,
                           String[] additionalHeaders, Stream<AuditEvent> data) throws IOException {
        try (var buffer = new BufferedOutputStream(outputStream);
             var outputWriter = new OutputStreamWriter(buffer, StandardCharsets.UTF_8);
             var printer = new CSVPrinter(outputWriter, CSVFormat.Builder.create()
                     .setHeader(headers)
                     .setAutoFlush(true)
                     .setNullString("")
                     .build())) {
            data.forEach(audit -> {
                try {
                    String[] parsedData = parseData(audit, zonedDateTimeConverter, exportDataConverter, additionalHeaders);
                    entityManager.detach(audit);

                    parsedData = malformedDateConverter.correctDateFields(parsedData);
                    printer.printRecord((Object[]) parsedData);
                    printer.flush();
                } catch (IOException e) {
                    throw new ApplicationExceptions.AuditExportException(e, EXPORT_FAILURE_CASE_DATA_DYNAMIC_ROW, "Unable to export case data for audit event %s", audit.getUuid());
                }
            });
        }
    }

    @Override
    protected void printData(OutputStream outputStream, ZonedDateTimeConverter zonedDateTimeConverter, ExportDataConverter exportDataConverter, boolean convertHeader, Stream<AuditEvent> data, String[] headers) {
    }

    @Override
    protected void printData(OutputStream outputStream, ZonedDateTimeConverter zonedDateTimeConverter, ExportDataConverter exportDataConverter, String[] headers, Stream<AuditEvent> data) {
    }

    @Override
    protected String[] parseData(AuditEvent audit,
                                 ZonedDateTimeConverter zonedDateTimeConverter, ExportDataConverter exportDataConverter)
            throws JsonProcessingException {
        return new String[0];
    }

    protected abstract String[] parseData(AuditEvent audit,
                                          ZonedDateTimeConverter zonedDateTimeConverter, ExportDataConverter exportDataConverter,
                                          String[] additionalHeaders)
            throws JsonProcessingException;

    abstract String[] getAdditionalHeaders(CaseTypeDto caseType);

}
