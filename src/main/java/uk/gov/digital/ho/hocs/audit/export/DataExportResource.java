package uk.gov.digital.ho.hocs.audit.export;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

@Slf4j
@RestController("/export")
public class DataExportResource {

    private ExportService exportService;

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
             log.error("Error exporting CSV file for case type {} and export type {} for reason {}", caseType, exportType.toString(), ex.toString());
             response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
         }
    }

    private String getFileName(String caseType, ExportType exportType) {
        return String.format("%s-%s.csv",caseType.toLowerCase(), exportType.toString().toLowerCase());
    }
}
