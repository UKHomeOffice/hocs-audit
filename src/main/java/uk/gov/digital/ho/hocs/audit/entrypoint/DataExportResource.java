package uk.gov.digital.ho.hocs.audit.entrypoint;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.digital.ho.hocs.audit.core.LogEvent;
import uk.gov.digital.ho.hocs.audit.core.exception.InvalidExportTypeException;
import uk.gov.digital.ho.hocs.audit.core.utils.ZonedDateTimeConverter;
import uk.gov.digital.ho.hocs.audit.service.DynamicExportService;
import uk.gov.digital.ho.hocs.audit.service.SomuExportService;
import uk.gov.digital.ho.hocs.audit.service.domain.ExportType;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.CSV_EXPORT_COMPLETE;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.CSV_EXPORT_START;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.EVENT;

@Slf4j
@RestController
public class DataExportResource {

    private final Map<ExportType, DynamicExportService> dynamicExportServices;
    private final SomuExportService somuExportService;

    public DataExportResource(List<DynamicExportService> dynamicExportServices, SomuExportService somuExportService) {
        this.dynamicExportServices = dynamicExportServices.stream()
                .collect(Collectors.toMap(DynamicExportService::getExportType, Function.identity()));
        this.somuExportService = somuExportService;
    }

    @GetMapping(value = "/export/{caseType}", params = {"fromDate", "exportType"}, produces = "text/csv")
    public @ResponseBody
    void getDataExport(@RequestParam("fromDate") LocalDate fromDate,
                       @RequestParam(name = "toDate", defaultValue = "#{T(java.time.LocalDate).now()}") LocalDate toDate,
                       @PathVariable("caseType") String caseType,
                       @RequestParam("exportType") ExportType exportType,
                       @RequestParam(name = "convert", defaultValue = "false") boolean convert,
                       @RequestParam(name = "convertHeader", defaultValue = "false") boolean convertHeader,
                       @RequestParam(name = "timestampFormat", required = false) String timestampFormat,
                       @RequestParam(name = "timeZoneId", required = false) String timeZoneId,
                       HttpServletResponse response) {
        ZonedDateTimeConverter zonedDateTimeConverter = new ZonedDateTimeConverter(timestampFormat, timeZoneId);

        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=" + getFileName(caseType, exportType));

        var service = dynamicExportServices.get(exportType);

        if (service == null) {
            throw new InvalidExportTypeException("Export service does not exist for type: %s", LogEvent.INVALID_PARAMETER_SPECIFIED, exportType);
        }

        try {
            log.info("Exporting {} to CSV", exportType, value(EVENT, CSV_EXPORT_START));
            service.export(fromDate, toDate, response.getWriter(), caseType, convert, convertHeader, zonedDateTimeConverter);
            log.info("Completed export of {} to CSV", exportType, value(EVENT, CSV_EXPORT_COMPLETE));
        } catch (Exception ex) {
            log.error("Error exporting CSV file for case type {} and export type {} for reason {}", caseType, exportType.toString(), ex.toString());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @GetMapping(value = "/export/somu/{caseType}", params = {"fromDate", "somuType"}, produces = "text/csv")
    public @ResponseBody
    void getSomuExport(@RequestParam("fromDate") LocalDate fromDate,
                       @RequestParam(value = "toDate", defaultValue = "#{T(java.time.LocalDate).now()}") LocalDate toDate,
                       @PathVariable("caseType") String caseType,
                       @RequestParam("somuType") String somuType,
                       @RequestParam(name = "convert", defaultValue = "false") boolean convert,
                       @RequestParam(name = "timestampFormat", required = false) String timestampFormat,
                       @RequestParam(name = "timeZoneId", required = false) String timeZoneId,
                       HttpServletResponse response) {
        ZonedDateTimeConverter zonedDateTimeConverter = new ZonedDateTimeConverter(timestampFormat, timeZoneId);

        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=" + getFileName(caseType, somuType));

        try {
            log.info("Exporting {}:{} to CSV", caseType, somuType, value(EVENT, CSV_EXPORT_START));
            somuExportService.export(fromDate, toDate, response.getWriter(), caseType, somuType, convert, zonedDateTimeConverter);
            log.info("Completed export of {}:{} to CSV", caseType, somuType, value(EVENT, CSV_EXPORT_COMPLETE));
        } catch (Exception ex) {
            log.error("Error exporting CSV file for case type {} and somu type {} for reason {}", caseType, somuType, ex.toString());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    //TODO: Remove somehow - is it actually useful?
    private String getFileName(String caseType, ExportType exportType) {
        return String.format("%s-%s-%s.csv", caseType.toLowerCase(), exportType.toString().toLowerCase(), LocalDate.now());
    }

    private String getFileName(String caseType, String export) {
        return String.format("%s-%s-%s.csv", caseType.toLowerCase(), export, LocalDate.now());
    }
}
