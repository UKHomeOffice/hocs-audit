package uk.gov.digital.ho.hocs.audit.entrypoint;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.digital.ho.hocs.audit.core.exception.AuditExportException;
import uk.gov.digital.ho.hocs.audit.core.exception.InvalidExportTypeException;
import uk.gov.digital.ho.hocs.audit.core.utils.ZonedDateTimeConverter;
import uk.gov.digital.ho.hocs.audit.service.DynamicExportService;
import uk.gov.digital.ho.hocs.audit.service.SomuExportService;
import uk.gov.digital.ho.hocs.audit.service.domain.ExportType;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static uk.gov.digital.ho.hocs.audit.core.LogEvent.DYNAMIC_EXPORT_FAILURE;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.INVALID_PARAMETER_SPECIFIED;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.SOMU_EXPORT_FAILURE;
import static uk.gov.digital.ho.hocs.audit.core.utils.FileNameHelper.getFileName;

@Slf4j
@RestController
public class DataExportResource {

    private final Map<ExportType, DynamicExportService> dynamicExportServices;
    private final SomuExportService somuExportService;

    public DataExportResource(List<DynamicExportService> dynamicExportServices, SomuExportService somuExportService) {
        this.dynamicExportServices = dynamicExportServices.stream().collect(Collectors.toMap(DynamicExportService::getExportType, Function.identity()));
        this.somuExportService = somuExportService;
    }

    @GetMapping(value = "/export/{caseType}", params = {"fromDate", "exportType"}, produces = "text/csv;charset=UTF-8")
    public void getDataExport(@RequestParam("fromDate") LocalDate fromDate,
                       @RequestParam(name = "toDate", defaultValue = "#{T(java.time.LocalDate).now()}") LocalDate toDate,
                       @PathVariable("caseType") String caseType,
                       @RequestParam("exportType") ExportType exportType,
                       @RequestParam(name = "convert", defaultValue = "false") boolean convert,
                       @RequestParam(name = "convertHeader", defaultValue = "false") boolean convertHeader,
                       @RequestParam(name = "timestampFormat", required = false) String timestampFormat,
                       @RequestParam(name = "timeZoneId", required = false) String timeZoneId,
                       HttpServletResponse response) {
        ZonedDateTimeConverter zonedDateTimeConverter = new ZonedDateTimeConverter(timestampFormat, timeZoneId);

        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + getFileName(caseType, exportType));

        var service = dynamicExportServices.get(exportType);

        if (service == null) {
            throw new InvalidExportTypeException(INVALID_PARAMETER_SPECIFIED, "Export service does not exist for type: %s", exportType);
        }

        try {
            service.export(fromDate, toDate, response.getOutputStream(), caseType, convert, convertHeader, zonedDateTimeConverter);
        } catch (IOException e) {
            throw new AuditExportException(e, DYNAMIC_EXPORT_FAILURE, "Unable to export Dynamic Data");
        }
    }

    @GetMapping(value = "/export/somu/{caseType}", params = {"fromDate", "somuType"}, produces = "text/csv;charset=UTF-8")
    public void getSomuExport(@RequestParam("fromDate") LocalDate fromDate,
                       @RequestParam(value = "toDate", defaultValue = "#{T(java.time.LocalDate).now()}") LocalDate toDate,
                       @PathVariable("caseType") String caseType,
                       @RequestParam("somuType") String somuType,
                       @RequestParam(name = "convert", defaultValue = "false") boolean convert,
                       @RequestParam(name = "timestampFormat", required = false) String timestampFormat,
                       @RequestParam(name = "timeZoneId", required = false) String timeZoneId,
                       HttpServletResponse response) {
        ZonedDateTimeConverter zonedDateTimeConverter = new ZonedDateTimeConverter(timestampFormat, timeZoneId);

        try {
            setResponseHeaders(response, getFileName(caseType, somuType));
            somuExportService.export(fromDate, toDate, response.getOutputStream(), caseType, somuType, convert, zonedDateTimeConverter);
        } catch (IOException e) {
            throw new AuditExportException(e, SOMU_EXPORT_FAILURE, "Unable to export Somu Data");
        }
    }

    private void setResponseHeaders(HttpServletResponse response, String filename) throws IOException {
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=" + filename);
        response.setStatus(HttpStatus.OK.value());
        response.flushBuffer();
    }
}
