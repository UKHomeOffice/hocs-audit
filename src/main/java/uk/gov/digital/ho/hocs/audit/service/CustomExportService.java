package uk.gov.digital.ho.hocs.audit.service;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import uk.gov.digital.ho.hocs.audit.client.info.InfoClient;
import uk.gov.digital.ho.hocs.audit.client.info.dto.ExportViewDto;
import uk.gov.digital.ho.hocs.audit.core.RequestData;
import uk.gov.digital.ho.hocs.audit.core.exception.EntityPermissionException;
import uk.gov.digital.ho.hocs.audit.repository.AuditRepository;
import uk.gov.digital.ho.hocs.audit.service.domain.converter.CustomExportDataConverter;
import uk.gov.digital.ho.hocs.audit.service.domain.converter.HeaderConverter;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.CSV_EXPORT_FAILURE;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.EVENT;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.REFRESH_MATERIALISED_VIEW;

@Slf4j
@Service
public class CustomExportService {

    private final AuditRepository auditRepository;
    private final InfoClient infoClient;
    private final CustomExportDataConverter customExportDataConverter;
    private final HeaderConverter headerConverter;
    private final RequestData requestData;

    public CustomExportService(AuditRepository auditRepository, InfoClient infoClient, CustomExportDataConverter customExportDataConverter, HeaderConverter headerConverter, RequestData requestData) {
        this.auditRepository = auditRepository;
        this.infoClient = infoClient;
        this.customExportDataConverter = customExportDataConverter;
        this.headerConverter = headerConverter;
        this.requestData = requestData;
    }

    @Transactional(readOnly = true, timeout = 300)
    public void customExport(HttpServletResponse response, String viewName, boolean convertHeader) throws IOException {
        ExportViewDto exportViewDto = infoClient.getExportView(viewName);

        if (StringUtils.hasText(exportViewDto.getRequiredPermission()) &&
                !requestData.roles().contains(exportViewDto.getRequiredPermission())) {
            // TODO: remove the log and add to the entity permission error with suitable LogEvent
            log.error("Cannot export due to permission not assigned to the user, user {}, permission {}", requestData.userId(), exportViewDto.getRequiredPermission());
            throw new EntityPermissionException("No permission to view %s", viewName);
        }

        String[] headers = getHeaders(exportViewDto, convertHeader);

        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + getFilename(exportViewDto.getDisplayName(), viewName));
        customExportDataConverter.initialiseAdapters();

        try (var printer =
                     new CSVPrinter(response.getWriter(), CSVFormat.Builder.create()
                             .setHeader(headers)
                             .setAutoFlush(true)
                             .build())) {
            AtomicBoolean connected = new AtomicBoolean(true);

            retrieveAuditData(exportViewDto.getCode())
                    .parallel()
                    .map(data -> {
                        Object[] converted = customExportDataConverter.convertData(data, exportViewDto.getFields());

                        if (converted == null) {
                            log.warn("No data to print after converting data {}", data);
                            return new Object[0];
                        }

                        return converted;
                    })
                    .takeWhile(c -> connected.get())
                    .forEachOrdered(converted -> {
                        try {
                            printer.printRecord(converted);
                        } catch (IOException e) {
                            connected.set(false);
                            log.error("Unable to parse record for custom report, reason: {}, event: {}", e.getMessage(), value(EVENT, CSV_EXPORT_FAILURE));
                        }
                    });
        }
    }

    private String[] getHeaders(ExportViewDto exportView, boolean convertHeader) {
        String[] headers = customExportDataConverter.getHeaders(exportView);

        if (convertHeader) {
            return headerConverter.substitute(headers);
        }

        return headers;
    }

    Stream<Object[]> retrieveAuditData(@NonNull String exportViewCode) {
        return auditRepository
                .getResultsFromView(exportViewCode);
    }

    //TODO: this probably isn't required
    @Transactional
    public void refreshMaterialisedView(String viewName) {
        log.info("Refreshing materialise view '{}', event {}", viewName, value(EVENT, REFRESH_MATERIALISED_VIEW));
        auditRepository.refreshMaterialisedView(viewName);
    }

    @Transactional
    public LocalDate getViewLastRefreshedDate(String viewName) {
        return auditRepository.getViewLastRefreshedDate(viewName);
    }

    public String getFilename(String displayName, String viewName) {
        return String.format("%s-%s.csv", displayName, getViewLastRefreshedDate(viewName).toString());
    }
}
