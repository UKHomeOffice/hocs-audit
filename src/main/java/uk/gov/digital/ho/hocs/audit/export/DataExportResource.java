package uk.gov.digital.ho.hocs.audit.export;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.digital.ho.hocs.audit.auditdetails.exception.EntityPermissionException;

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
                       @RequestParam(name = "convert", defaultValue = "false") boolean convert,
                       @RequestParam(name = "convertHeader", defaultValue = "false") boolean convertHeader,
                       @RequestParam(name = "timestampFormat", required = false) String timestampFormat,
                       @RequestParam(name = "timeZoneId", required = false) String timeZoneId,
                       HttpServletResponse response) {
        try {
            response.setContentType("text/csv");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=" + getFileName(caseType, exportType));
            exportService.auditExport(fromDate, toDate, response.getOutputStream(), caseType, exportType, convert, convertHeader, timestampFormat, timeZoneId);
            response.setStatus(200);
        } catch (Exception ex) {
            log.error("Error exporting CSV file for case type {} and export type {} for reason {}", caseType, exportType.toString(), ex.toString());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @GetMapping(value = "/export/somu/{caseType}", params = {"fromDate", "toDate", "somuType"})
    public @ResponseBody
    void getSomuExport(@RequestParam("fromDate") LocalDate fromDate,
                       @RequestParam("toDate") LocalDate toDate,
                       @PathVariable("caseType") String caseType,
                       @RequestParam("somuType") String somuType,
                       @RequestParam(name = "timestampFormat", required = false) String timestampFormat,
                       @RequestParam(name = "timeZoneId", required = false) String timeZoneId,
                       HttpServletResponse response) {
        try {
            response.setContentType("text/csv");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=" + getFileName(caseType, somuType));
            exportService.auditSomuExport(fromDate, toDate, response.getOutputStream(), caseType, somuType, timestampFormat, timeZoneId);
            response.setStatus(200);
        } catch (Exception ex) {
            log.error("Error exporting CSV file for case type {} and somu type {} for reason {}", caseType, somuType, ex.toString());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @GetMapping("/export/topics")
    public void getTopics(HttpServletResponse response,
                          @RequestParam(name = "convertHeader", defaultValue = "false") boolean convertHeader) {
        try {
            response.setContentType("text/csv");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=" + getFilename("topics"));
            exportService.staticTopicExport(response.getOutputStream(), convertHeader);
            response.setStatus(200);
        } catch (Exception ex) {
            log.error("Error exporting CSV file for static topic list for reason {}", ex.toString());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @GetMapping("/export/topics/{caseType}/teams")
    public void getTopicsWithTeams(@PathVariable ("caseType") String caseType,
                                   HttpServletResponse response,
                                   @RequestParam (name = "convertHeader", defaultValue = "false") boolean convertHeader) {
        try {
            response.setContentType("text/csv");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=" + getFileName(caseType, "topics_teams"));
            exportService.staticTopicsWithTeamsExport(response.getOutputStream(), caseType, convertHeader);
            response.setStatus(200);
        } catch (Exception ex) {
            log.error("Error exporting CSV file for static topic list for reason {}", ex.toString());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @GetMapping("/export/teams")
    public void getTeams(HttpServletResponse response,
                         @RequestParam(name = "convertHeader", defaultValue = "false") boolean convertHeader) {
        try {
            response.setContentType("text/csv");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=" + getFilename("teams"));
            exportService.staticTeamExport(response.getOutputStream(), convertHeader);
            response.setStatus(200);
        } catch (Exception ex) {
            log.error("Error exporting CSV file for static team list for reason {}", ex.toString());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @GetMapping("/export/units/teams")
    public void getUnitsForTeams(HttpServletResponse response,
                                 @RequestParam(name = "convertHeader", defaultValue = "false") boolean convertHeader) {
        try {
            response.setContentType("text/csv");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=" + getFilename("units_teams"));
            exportService.staticUnitsForTeamsExport(response.getOutputStream(), convertHeader);
            response.setStatus(200);
        } catch (Exception ex) {
            log.error("Error exporting CSV file for static units for teams list for reason {}", ex.toString());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @GetMapping("/export/users")
    public void getUsers(HttpServletResponse response,
                         @RequestParam(name = "convertHeader", defaultValue = "false") boolean convertHeader) {
        try {
            response.setContentType("text/csv");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=" + getFilename("users"));
            exportService.staticUserExport(response.getOutputStream(), convertHeader);
            response.setStatus(200);
        } catch (Exception ex) {
            log.error("Error exporting CSV file for static user list for reason {}", ex.toString());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @GetMapping(value = "/export/custom/{code}")
    public @ResponseBody
    void getCustomDataExport(@PathVariable("code") String code, HttpServletResponse response,
                             @RequestParam(name = "convertHeader", defaultValue = "false") boolean convertHeader) {

        try {
            customExportService.customExport(response, code, convertHeader);
            response.setStatus(200);
        } catch (Exception ex) {
            log.error("Error exporting CSV file for custom report {}: {}", code, ex.getMessage());
            if (ex instanceof HttpClientErrorException) {
                response.setStatus(((HttpClientErrorException) ex).getRawStatusCode());
            } else if (ex instanceof EntityPermissionException) {
                response.setStatus(HttpStatus.FORBIDDEN.value());
            } else {
                response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            }
        }

    }

    private String getFileName(String caseType, ExportType exportType) {
        return String.format("%s-%s-%s.csv", caseType.toLowerCase(), exportType.toString().toLowerCase(), LocalDate.now().toString());
    }

    private String getFileName(String caseType, String export) {
        return String.format("%s-%s-%s.csv", caseType.toLowerCase(), export, LocalDate.now().toString());
    }

    private String getFilename(String export) {
        return String.format("%s-%s.csv", export, LocalDate.now().toString());
    }
}
