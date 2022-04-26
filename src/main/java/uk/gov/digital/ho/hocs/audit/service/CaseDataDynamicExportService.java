package uk.gov.digital.ho.hocs.audit.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.audit.client.casework.CaseworkClient;
import uk.gov.digital.ho.hocs.audit.client.info.InfoClient;
import uk.gov.digital.ho.hocs.audit.core.LogEvent;
import uk.gov.digital.ho.hocs.audit.core.exception.AuditExportException;
import uk.gov.digital.ho.hocs.audit.core.utils.ZonedDateTimeConverter;
import uk.gov.digital.ho.hocs.audit.entrypoint.dto.AuditPayload;
import uk.gov.digital.ho.hocs.audit.repository.AuditRepository;
import uk.gov.digital.ho.hocs.audit.repository.entity.AuditEvent;
import uk.gov.digital.ho.hocs.audit.service.domain.ExportType;
import uk.gov.digital.ho.hocs.audit.service.domain.converter.ExportDataConverter;
import uk.gov.digital.ho.hocs.audit.service.domain.converter.HeaderConverter;
import uk.gov.digital.ho.hocs.audit.service.domain.converter.MalformedDateConverter;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.CSV_EXPORT_FAILURE;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.CSV_RECORD_EXPORT_FAILURE;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.EVENT;

@Slf4j
@Service
public abstract class CaseDataDynamicExportService extends DynamicExportService {

    public CaseDataDynamicExportService(ObjectMapper objectMapper, AuditRepository auditRepository,
                                        InfoClient infoClient, CaseworkClient caseworkClient,
                                        HeaderConverter headerConverter, MalformedDateConverter malformedDateConverter) {
        super(objectMapper, auditRepository, infoClient, caseworkClient, headerConverter, malformedDateConverter);
    }

    protected void printData(PrintWriter writer, ZonedDateTimeConverter zonedDateTimeConverter,
                             ExportDataConverter exportDataConverter, boolean convertHeader, String caseType,
                             Stream<AuditEvent> data) {
        var concatenatedHeaders = Stream.concat(Arrays.stream(getHeaders()), Arrays.stream(getAdditionalHeaders(caseType)))
                .toArray(String[]::new);

        printData(writer, zonedDateTimeConverter, exportDataConverter, convertHeader, caseType, data, concatenatedHeaders);
    }

    abstract String[] getAdditionalHeaders(String caseType);

}
