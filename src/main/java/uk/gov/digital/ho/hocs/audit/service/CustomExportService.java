package uk.gov.digital.ho.hocs.audit.service;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import uk.gov.digital.ho.hocs.audit.core.RequestData;
import uk.gov.digital.ho.hocs.audit.core.exception.EntityPermissionException;
import uk.gov.digital.ho.hocs.audit.repository.AuditRepository;
import uk.gov.digital.ho.hocs.audit.repository.config.CustomExportViewsReader;
import uk.gov.digital.ho.hocs.audit.repository.config.model.CustomExportViews;
import uk.gov.digital.ho.hocs.audit.service.domain.converter.CustomExportDataConverter;
import uk.gov.digital.ho.hocs.audit.service.domain.converter.HeaderConverter;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
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
    private final CustomExportViewsReader customExportViewsReader;
    private final CustomExportDataConverter customExportDataConverter;
    private final HeaderConverter headerConverter;
    private final RequestData requestData;

    public CustomExportService(AuditRepository auditRepository, CustomExportViewsReader customExportViewsReader, CustomExportDataConverter customExportDataConverter, HeaderConverter headerConverter, RequestData requestData) {
        this.auditRepository = auditRepository;
        this.customExportViewsReader = customExportViewsReader;
        this.customExportDataConverter = customExportDataConverter;
        this.headerConverter = headerConverter;
        this.requestData = requestData;
    }

    @Transactional(readOnly = true)
    public void export(HttpServletResponse response, String viewName, boolean convertHeader) throws IOException {
        var exportView = customExportViewsReader.getByViewName(viewName);

        if (StringUtils.hasText(exportView.getRequiredPermission()) &&
                !requestData.getRoles().contains(exportView.getRequiredPermission())) {
            // TODO: remove the log and add to the entity permission error with suitable LogEvent
            log.error("Cannot export due to permission not assigned to the user, user {}, permission {}", requestData.getUserId(), exportView.getRequiredPermission());
            throw new EntityPermissionException("No permission to view %s", viewName);
        }

        String[] headers = getHeaders(exportView, convertHeader);

        customExportDataConverter.initialiseAdapters();

        try (OutputStream buffer = new BufferedOutputStream(response.getOutputStream());
             OutputStreamWriter outputWriter = new OutputStreamWriter(buffer, StandardCharsets.UTF_8);
             var printer =
                     new CSVPrinter(outputWriter, CSVFormat.Builder.create()
                             .setHeader(headers)
                             .setAutoFlush(true)
                             .setNullString("")
                             .build())) {
            AtomicBoolean connected = new AtomicBoolean(true);

            retrieveAuditData(viewName)
                    .parallel()
                    .map(data -> {
                        Object[] converted = customExportDataConverter.convertData(data, exportView.getFields());

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

    private String[] getHeaders(CustomExportViews.CustomExportView exportView, boolean convertHeader) {
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

    @Transactional
    public void refreshMaterialisedView(String viewName) {
        log.info("Refreshing materialise view '{}', event {}", viewName, value(EVENT, REFRESH_MATERIALISED_VIEW));
        auditRepository.refreshMaterialisedView(viewName);
    }

    public LocalDate getViewLastRefreshedDate(String viewName) {
        return auditRepository.getViewLastRefreshedDate(viewName);
    }


}
