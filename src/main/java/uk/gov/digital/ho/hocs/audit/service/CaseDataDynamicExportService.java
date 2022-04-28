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
import uk.gov.digital.ho.hocs.audit.core.exception.AuditExportException;
import uk.gov.digital.ho.hocs.audit.core.utils.ZonedDateTimeConverter;
import uk.gov.digital.ho.hocs.audit.repository.AuditRepository;
import uk.gov.digital.ho.hocs.audit.repository.entity.AuditEvent;
import uk.gov.digital.ho.hocs.audit.service.domain.converter.ExportDataConverter;
import uk.gov.digital.ho.hocs.audit.service.domain.converter.HeaderConverter;
import uk.gov.digital.ho.hocs.audit.service.domain.converter.MalformedDateConverter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.stream.Stream;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.CSV_EXPORT_FAILURE;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.CSV_RECORD_EXPORT_FAILURE;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.EVENT;

@Slf4j
@Service
public abstract class CaseDataDynamicExportService extends DynamicExportService {

    protected CaseDataDynamicExportService(ObjectMapper objectMapper, AuditRepository auditRepository,
                                        InfoClient infoClient, CaseworkClient caseworkClient,
                                        HeaderConverter headerConverter, MalformedDateConverter malformedDateConverter) {
        super(objectMapper, auditRepository, infoClient, caseworkClient, headerConverter, malformedDateConverter);
    }

    protected void printData(PrintWriter writer, ZonedDateTimeConverter zonedDateTimeConverter,
                             ExportDataConverter exportDataConverter, boolean convertHeader, CaseTypeDto caseType,
                             Stream<AuditEvent> data) {
        var additionalHeaders = getAdditionalHeaders(caseType);
        var concatenatedHeaders = Stream.concat(Arrays.stream(getHeaders()), Arrays.stream(additionalHeaders))
                .toArray(String[]::new);

        if (convertHeader) {
            headerConverter.substitute(concatenatedHeaders);
        }

        printData(writer, zonedDateTimeConverter, exportDataConverter, concatenatedHeaders, additionalHeaders, data);
    }

    private void printData(PrintWriter writer, ZonedDateTimeConverter zonedDateTimeConverter,
                             ExportDataConverter exportDataConverter, String[] headers,
                             String[] additionalHeaders, Stream<AuditEvent> data) {
        try (var printer =
                     new CSVPrinter(writer, CSVFormat.Builder.create()
                             .setHeader(headers)
                             .setAutoFlush(true)
                             .build())) {
            data.forEach(audit -> {
                try {
                    String[] parsedData = parseData(audit, zonedDateTimeConverter, exportDataConverter, additionalHeaders);
                    entityManager.detach(audit);

                    parsedData = malformedDateConverter.correctDateFields(parsedData);
                    printer.printRecord((Object[]) parsedData);
                    printer.flush();
                } catch (IOException e) {
                    throw new AuditExportException("Unable to parse record for audit {} for reason {}", CSV_RECORD_EXPORT_FAILURE, audit.getUuid(), e.getMessage());
                }
            });
        } catch (IOException e) {
            log.error("Unable to export record for reason {}", e.getMessage(), value(EVENT, CSV_EXPORT_FAILURE));
        }
    }

    @Override
    protected void printData(PrintWriter writer, ZonedDateTimeConverter zonedDateTimeConverter, ExportDataConverter exportDataConverter, boolean convertHeader, Stream<AuditEvent> data, String[] headers) { }

    @Override
    protected void printData(PrintWriter writer, ZonedDateTimeConverter zonedDateTimeConverter, ExportDataConverter exportDataConverter, String[] headers, Stream<AuditEvent> data) { }

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
