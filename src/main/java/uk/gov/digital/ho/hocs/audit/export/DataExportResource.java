package uk.gov.digital.ho.hocs.audit.export;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

@Slf4j
@RestController()
public class DataExportResource {

    private ExportService exportService;
    private CustomExportService customExportService;

    public DataExportResource(ExportService exportService, CustomExportService customExportService) {
        this.exportService = exportService;
        this.customExportService = customExportService;
    }

    @GetMapping(value = "/export/{caseType}", params = {"fromDate", "toDate", "exportType"})
    public @ResponseBody
    void getDataExport(@RequestParam("fromDate") LocalDate fromDate, @RequestParam("toDate") LocalDate toDate,
                       @PathVariable("caseType") String caseType, @RequestParam("exportType") ExportType exportType,
                       HttpServletResponse response) {
        try {
            response.setContentType("text/csv");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=" + getFileName(caseType, exportType));
            exportService.auditExport(fromDate, toDate, response.getOutputStream(), caseType, exportType);
            response.setStatus(200);
        } catch (Exception ex) {
            log.error("Error exporting CSV file for case type {} and export type {} for reason {}", caseType, exportType.toString(), ex.toString());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @GetMapping("/export/topics")
    public void getTopics(HttpServletResponse response) {
        try {
            response.setContentType("text/csv");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=topics.csv");
            exportService.staticTopicExport(response.getOutputStream());
            response.setStatus(200);
        } catch (Exception ex) {
            log.error("Error exporting CSV file for static topic list for reason {}", ex.toString());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @GetMapping("/export/teams")
    public void getTeams(HttpServletResponse response) {
        try {
            response.setContentType("text/csv");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=teams.csv");
            exportService.staticTeamExport(response.getOutputStream());
            response.setStatus(200);
        } catch (Exception ex) {
            log.error("Error exporting CSV file for static team list for reason {}", ex.toString());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @GetMapping("/export/users")
    public void getUsers(HttpServletResponse response) {
        try {
            response.setContentType("text/csv");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=users.csv");
            exportService.staticUserExport(response.getOutputStream());
            response.setStatus(200);
        } catch (Exception ex) {
            log.error("Error exporting CSV file for static user list for reason {}", ex.toString());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @GetMapping(value = "/export/custom/{code}")
    public @ResponseBody
    void getCustomDataExport(@PathVariable("code") String code, HttpServletResponse response) {

        try {
            customExportService.customExport(response, code);
            response.setStatus(200);
        } catch (Exception ex) {
            log.error("Error exporting CSV file for custom report {}", code);
            if (ex instanceof HttpClientErrorException) {
                response.setStatus(((HttpClientErrorException) ex).getRawStatusCode());
            } else {
                response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            }
        }

    }

    private String getFileName(String caseType, ExportType exportType) {
        return String.format("%s-%s.csv", caseType.toLowerCase(), exportType.toString().toLowerCase());
    }
}
