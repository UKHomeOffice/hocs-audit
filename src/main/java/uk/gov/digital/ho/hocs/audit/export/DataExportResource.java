package uk.gov.digital.ho.hocs.audit.export;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.digital.ho.hocs.audit.export.ExportService;
import uk.gov.digital.ho.hocs.audit.export.ExportType;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

@Slf4j
@RestController("/uk.gov.digital.ho.hocs.audit.export")
public class DataExportResource {

    private ExportService exportService;

    DateTimeFormatter format = new DateTimeFormatterBuilder()
            .appendPattern("yyyy")
            .parseDefaulting(ChronoField.MONTH_OF_YEAR, 1)
            .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
            .toFormatter();

    public DataExportResource(ExportService exportService) {
        this.exportService = exportService;
    }

    @GetMapping( params = {"fromDate", "toDate", "caseType", "exportType"})
    public @ResponseBody void getDataExport(@RequestParam("fromDate") LocalDate fromDate, @RequestParam("toDate") LocalDate toDate,
                                            @RequestParam("caseType") String caseType, @RequestParam("exportType") ExportType exportType,
                                            HttpServletResponse response) {
        try {
             response.setContentType("text/csv");
             response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                     "attachment; filename=" + getFileName(caseType, exportType));
             exportService.auditExport(fromDate, toDate, response.getOutputStream(), caseType, exportType);
             response.setStatus(200);
         }
         catch(Exception ex) {
             log.error("Error exporting CSV file for case type {} and uk.gov.digital.ho.hocs.audit.export type {} for reason {}", caseType, exportType.toString(), ex.toString());
             response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
         }
    }

    private String getFileName(String caseType, ExportType exportType) {
        return String.format("%s-%s.csv",caseType.toLowerCase(), exportType.toString().toLowerCase());
    }
}
