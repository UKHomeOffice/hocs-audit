package uk.gov.digital.ho.hocs.audit.export;

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
import java.time.LocalDate;
import java.util.List;

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

    public CustomExportService(AuditRepository auditRepository, InfoClient infoClient, CustomExportDataConverter customExportDataConverter, HeaderConverter headerConverter, RequestData requestData) {
        this.auditRepository = auditRepository;
        this.infoClient = infoClient;
        this.customExportDataConverter = customExportDataConverter;
        this.headerConverter = headerConverter;
        this.requestData = requestData;
    }

    @Transactional(readOnly = true)
    public void customExport(HttpServletResponse response, String code, boolean convertHeader) throws IOException {
        ExportViewDto exportViewDto = infoClient.getExportView(code);

        if (StringUtils.hasText(exportViewDto.getRequiredPermission()) && !requestData.roles().contains(exportViewDto.getRequiredPermission())) {
            log.error("Cannot export due to permission not assigned to the user, user {}, permission {}", requestData.userId(), exportViewDto.getRequiredPermission());
            throw new EntityPermissionException("No permission to view %s", code);
        } else {
            response.setContentType("text/csv");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=" + getFilename(exportViewDto.getDisplayName()));


            OutputStream buffer = new BufferedOutputStream(response.getOutputStream());
            OutputStreamWriter outputWriter = new OutputStreamWriter(buffer, StandardCharsets.UTF_8);

            List<String> headers = customExportDataConverter.getHeaders(exportViewDto);
            List<String> substitutedHeaders = headers;
            if (convertHeader) {
                substitutedHeaders = headerConverter.substitute(headers);
            }
            List<Object[]> dataList = customExportDataConverter.convertData(auditRepository.getResultsFromView(exportViewDto.getCode()), exportViewDto.getFields());

            try (CSVPrinter printer = new CSVPrinter(outputWriter, CSVFormat.DEFAULT.withHeader(substitutedHeaders.toArray(new String[substitutedHeaders.size()])))) {

                dataList.forEach((dataRow) -> {
                    try {
                        printer.printRecord(dataRow);
                        outputWriter.flush();
                    } catch (Exception e) {
                        log.error("Unable to parse record for custom report, reason: {}, event: {}", e.getMessage(), value(LogEvent.EVENT, CSV_EXPORT_FAILURE));
                    }
                });
                log.info("Export Custom Report '{}' to CSV Complete, event {}", exportViewDto.getCode(), value(EVENT, CSV_EXPORT_COMPETE));
            }

        }

    }

    private String getFilename(String displayName) {
        return String.format("%s-%s.csv", displayName, LocalDate.now().toString());
    }
}


