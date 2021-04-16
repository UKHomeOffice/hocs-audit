package uk.gov.digital.ho.hocs.audit.export;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import uk.gov.digital.ho.hocs.audit.application.LogEvent;
import uk.gov.digital.ho.hocs.audit.application.RequestData;
import uk.gov.digital.ho.hocs.audit.auditdetails.exception.EntityPermissionException;
import uk.gov.digital.ho.hocs.audit.auditdetails.repository.AuditRepository;
import uk.gov.digital.ho.hocs.audit.export.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.audit.export.infoclient.dto.ExportViewDto;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.audit.application.LogEvent.*;

@Slf4j
@Service
public class CustomExportService {

    private AuditRepository auditRepository;
    private InfoClient infoClient;
    private CustomExportDataConverter customExportDataConverter;
    private final HeaderConverter headerConverter;
    private RequestData requestData;

    private final String REFRESH_MATERIALISED_VIEW_ROLE = "REFRESH_MATERIALISED_VIEW_USER";

    public CustomExportService(AuditRepository auditRepository, InfoClient infoClient, CustomExportDataConverter customExportDataConverter, HeaderConverter headerConverter, RequestData requestData) {
        this.auditRepository = auditRepository;
        this.infoClient = infoClient;
        this.customExportDataConverter = customExportDataConverter;
        this.headerConverter = headerConverter;
        this.requestData = requestData;
    }

    @Transactional(readOnly = true, timeout = 300)
    public void customExport(HttpServletResponse response, String code, boolean convertHeader) throws IOException {
        ExportViewDto exportViewDto = infoClient.getExportView(code);

        if (StringUtils.hasText(exportViewDto.getRequiredPermission()) &&
                !requestData.roles().contains(exportViewDto.getRequiredPermission())) {
            log.error("Cannot export due to permission not assigned to the user, user {}, permission {}", requestData.userId(), exportViewDto.getRequiredPermission());
            throw new EntityPermissionException("No permission to view %s", code);
        } else {
            response.setContentType("text/csv;charset=UTF-8");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + getFilename(exportViewDto.getDisplayName(), code));

            OutputStream buffer = new BufferedOutputStream(response.getOutputStream());
            OutputStreamWriter outputWriter = new OutputStreamWriter(buffer, StandardCharsets.UTF_8);

            List<String> headers = customExportDataConverter.getHeaders(exportViewDto);
            List<String> substitutedHeaders = headers;
            if (convertHeader) {
                substitutedHeaders = headerConverter.substitute(headers);
            }

            try (CSVPrinter printer =
                         new CSVPrinter(outputWriter, CSVFormat.DEFAULT.withHeader(substitutedHeaders.toArray(new String[substitutedHeaders.size()])))) {
                // Immediately flush the output writer so the file starts downloading immediately.
                outputWriter.flush();

                customExportDataConverter.initialiseAdapters();

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
                                outputWriter.flush();
                            } catch (IOException e) {
                                log.error("Unable to parse record for custom report, reason: {}, event: {}", e.getMessage(), value(LogEvent.EVENT, CSV_EXPORT_FAILURE));
                            }
                        });
            }

            log.info("Export Custom Report '{}' to CSV Complete, event {}", exportViewDto.getCode(), value(EVENT, CSV_EXPORT_COMPETE));
        }
    }

    Stream<Object[]> retrieveAuditData(@NonNull String exportViewCode) {
        return auditRepository
                .getResultsFromView(exportViewCode);
    }


    @Transactional
    public int refreshMaterialisedView(final String viewName) {
        if (!requestData.roles().contains(REFRESH_MATERIALISED_VIEW_ROLE)) {
            log.error("Cannot export due to permission not assigned to the user, user {}, permission REFRESH_MATERIALISED_VIEW_USER", requestData.userId(), REFRESH_MATERIALISED_VIEW_ROLE);
            throw new EntityPermissionException("No permission to refresh materialised view");
        } else {
            log.info("Refreshing materialise view '{}', event {}", viewName, value(EVENT, REFRESH_MATERIALISED_VIEW));
            return auditRepository.refreshMaterialisedView(viewName);
        }
    }

    @Transactional
    public LocalDate getViewLastRefreshedDate(final String viewName) {
        return auditRepository.getViewLastRefreshedDate(viewName);
    }

    public String getFilename(String displayName, String viewName) {
        return String.format("%s-%s.csv", displayName, getViewLastRefreshedDate(viewName).toString());
    }
}


