package uk.gov.digital.ho.hocs.audit.service;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import uk.gov.digital.ho.hocs.audit.client.info.InfoClient;
import uk.gov.digital.ho.hocs.audit.client.info.dto.ExportViewDto;
import uk.gov.digital.ho.hocs.audit.core.RequestData;
import uk.gov.digital.ho.hocs.audit.core.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.audit.repository.AuditRepository;
import uk.gov.digital.ho.hocs.audit.service.domain.converter.CustomExportDataConverter;
import uk.gov.digital.ho.hocs.audit.service.domain.converter.HeaderConverter;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.CSV_EXPORT_COMPLETE;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.CSV_EXPORT_START;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.EXPORT_FAILURE_CUSTOM_ROW;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.EVENT;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.INVALID_EXPORT_PERMISSION;
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

    @Transactional(readOnly = true)
    public void export(HttpServletResponse response, String viewName, boolean convertHeader) throws IOException {
        log.info("Exporting {} to CSV", viewName, value(EVENT, CSV_EXPORT_START));
        ExportViewDto exportViewDto = infoClient.getExportView(viewName);

        if (StringUtils.hasText(exportViewDto.getRequiredPermission()) &&
                !requestData.getRoles().contains(exportViewDto.getRequiredPermission())) {
            throw new ApplicationExceptions.EntityPermissionException(INVALID_EXPORT_PERMISSION, "No permission to view %s for user %s", viewName, requestData.getUserId());
        }

        customExportDataConverter.initialiseAdapters();

        try (var buffer = new BufferedOutputStream(response.getOutputStream());
             var outputWriter = new OutputStreamWriter(buffer, StandardCharsets.UTF_8);
             var printer = new CSVPrinter(outputWriter, CSVFormat.Builder.create()
                     .setHeader(getHeaders(exportViewDto, convertHeader))
                     .setAutoFlush(true)
                     .setNullString("")
                     .build())) {

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
                    .forEachOrdered(converted -> {
                        try {
                            printer.printRecord(converted);
                        }
                        catch (IOException e) {
                            throw new ApplicationExceptions.AuditExportException(e, EXPORT_FAILURE_CUSTOM_ROW, "Unable to export Custom Data for %s", viewName);
                        }
                    });
            log.info("Completed {} to CSV", viewName, value(EVENT, CSV_EXPORT_COMPLETE));
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

    @Transactional
    public void refreshMaterialisedView(String viewName) {
        log.info("Refreshing materialise view '{}', event {}", viewName, value(EVENT, REFRESH_MATERIALISED_VIEW));
        auditRepository.refreshMaterialisedView(viewName);
    }


}
