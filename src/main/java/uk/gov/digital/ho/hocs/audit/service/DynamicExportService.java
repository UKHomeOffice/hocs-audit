package uk.gov.digital.ho.hocs.audit.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.digital.ho.hocs.audit.client.casework.CaseworkClient;
import uk.gov.digital.ho.hocs.audit.client.info.InfoClient;
import uk.gov.digital.ho.hocs.audit.client.info.dto.CaseTypeDto;
import uk.gov.digital.ho.hocs.audit.core.LogEvent;
import uk.gov.digital.ho.hocs.audit.core.exception.AuditExportException;
import uk.gov.digital.ho.hocs.audit.core.utils.ZonedDateTimeConverter;
import uk.gov.digital.ho.hocs.audit.repository.AuditRepository;
import uk.gov.digital.ho.hocs.audit.repository.entity.AuditEvent;
import uk.gov.digital.ho.hocs.audit.service.domain.ExportType;
import uk.gov.digital.ho.hocs.audit.service.domain.converter.ExportDataConverter;
import uk.gov.digital.ho.hocs.audit.service.domain.converter.HeaderConverter;
import uk.gov.digital.ho.hocs.audit.service.domain.converter.MalformedDateConverter;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.stream.Stream;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.CSV_EXPORT_FAILURE;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.CSV_RECORD_EXPORT_FAILURE;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.EVENT;

@Slf4j
@Service
public abstract class DynamicExportService {

    protected final ObjectMapper objectMapper;

    protected final AuditRepository auditRepository;

    protected final InfoClient infoClient;

    protected final CaseworkClient caseworkClient;

    protected final HeaderConverter headerConverter;

    protected final MalformedDateConverter malformedDateConverter;

    @PersistenceContext
    protected EntityManager entityManager;

    protected DynamicExportService(ObjectMapper objectMapper,
                                   AuditRepository auditRepository,
                                   InfoClient infoClient,
                                   CaseworkClient caseworkClient,
                                   HeaderConverter headerConverter,
                                   MalformedDateConverter malformedDateConverter) {
        this.objectMapper = objectMapper;
        this.auditRepository = auditRepository;
        this.infoClient = infoClient;
        this.caseworkClient = caseworkClient;
        this.headerConverter = headerConverter;
        this.malformedDateConverter = malformedDateConverter;
    }

    CaseTypeDto getCaseTypeCode(String caseType) {
        return infoClient.getCaseTypes().stream().filter(
            caseTypeDto -> caseTypeDto.getType().equals(caseType)).findFirst().orElseThrow(
            () -> new AuditExportException("Invalid case type specified %s", LogEvent.INVALID_CASE_TYPE_SPECIFIED,
                caseType));
    }

    protected void printData(OutputStream outputStream,
                             ZonedDateTimeConverter zonedDateTimeConverter,
                             ExportDataConverter exportDataConverter,
                             boolean convertHeader,
                             Stream<AuditEvent> data) {
        var headers = getConvertedHeaders(getHeaders(), convertHeader);

        printData(outputStream, zonedDateTimeConverter, exportDataConverter, headers, data);
    }

    protected void printData(OutputStream outputStream,
                             ZonedDateTimeConverter zonedDateTimeConverter,
                             ExportDataConverter exportDataConverter,
                             boolean convertHeader,
                             Stream<AuditEvent> data,
                             String[] headers) {
        var convertedHeaders = getConvertedHeaders(headers, convertHeader);

        printData(outputStream, zonedDateTimeConverter, exportDataConverter, convertedHeaders, data);
    }

    protected void printData(OutputStream outputStream,
                             ZonedDateTimeConverter zonedDateTimeConverter,
                             ExportDataConverter exportDataConverter,
                             String[] headers,
                             Stream<AuditEvent> data) {
        try (OutputStream buffer = new BufferedOutputStream(
            outputStream); OutputStreamWriter outputWriter = new OutputStreamWriter(buffer,
            StandardCharsets.UTF_8); var printer = new CSVPrinter(outputWriter,
            CSVFormat.Builder.create().setHeader(headers).setAutoFlush(true).setNullString("").build())) {
            data.forEach(audit -> {
                try {
                    String[] parsedData = parseData(audit, zonedDateTimeConverter, exportDataConverter);
                    entityManager.detach(audit);

                    parsedData = malformedDateConverter.correctDateFields(parsedData);
                    printer.printRecord((Object[]) parsedData);
                    printer.flush();
                } catch (IOException e) {
                    throw new AuditExportException(String.format("Unable to parse record for audit %s for reason %s", audit.getUuid(), e.getMessage()),
                        CSV_RECORD_EXPORT_FAILURE, e);
                }
            });
        } catch (IOException e) {
            log.error("Unable to export record for reason {}", e.getMessage(), value(EVENT, CSV_EXPORT_FAILURE));
        }
    }

    private String[] getConvertedHeaders(String[] headers, boolean convertHeader) {
        if (!convertHeader) {
            return headers;
        }

        return headerConverter.substitute(headers);
    }

    public abstract ExportType getExportType();

    protected abstract String[] parseData(AuditEvent audit,
                                          ZonedDateTimeConverter zonedDateTimeConverter,
                                          ExportDataConverter exportDataConverter) throws JsonProcessingException;

    @Transactional(readOnly = true)
    public abstract void export(LocalDate from,
                                LocalDate to,
                                OutputStream outputStream,
                                String caseType,
                                boolean convert,
                                boolean convertHeader,
                                ZonedDateTimeConverter zonedDateTimeConverter) throws IOException;

    protected abstract String[] getHeaders();

    protected abstract ExportDataConverter getDataConverter(boolean convert, CaseTypeDto caseType);

    protected abstract Stream<AuditEvent> getData(LocalDate from, LocalDate to, String caseTypeCode, String[] events);

}
