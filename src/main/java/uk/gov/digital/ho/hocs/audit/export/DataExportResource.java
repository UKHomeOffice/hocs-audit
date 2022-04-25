package uk.gov.digital.ho.hocs.audit.export;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.digital.ho.hocs.audit.auditdetails.exception.EntityPermissionException;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;

@Slf4j
@RestController()
public class DataExportResource {

    private final ExportService exportService;
    private final CustomExportService customExportService;

    public DataExportResource(ExportService exportService, CustomExportService customExportService) {
        this.exportService = exportService;
        this.customExportService = customExportService;
    }

    @GetMapping(value = "/export/{caseType}", params = {"fromDate", "exportType"})
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
            setResponseHeaders(getFileName(caseType, exportType), response);
            exportService.auditExport(fromDate, toDate, response), caseType, exportType, convert, convertHeader, timestampFormat, timeZoneId);
    }

    @GetMapping(value = "/export/somu/{caseType}", params = {"fromDate", "somuType"})
    public @ResponseBody
    void getSomuExport(@RequestParam("fromDate") LocalDate fromDate,
                       @RequestParam(value = "toDate", defaultValue = "#{T(java.time.LocalDate).now()}") LocalDate toDate,
                       @PathVariable("caseType") String caseType,
                       @RequestParam("somuType") String somuType,
                       @RequestParam(name = "convert", defaultValue = "false") boolean convert,
                       @RequestParam(name = "timestampFormat", required = false) String timestampFormat,
                       @RequestParam(name = "timeZoneId", required = false) String timeZoneId,
                       HttpServletResponse response) {
        setResponseHeaders(getFileName(caseType, somuType), response);
        exportService.auditSomuExport(fromDate, toDate, response, caseType, somuType, convert, timestampFormat, timeZoneId);
    }

    @GetMapping("/export/topics")
    public void getTopics(HttpServletResponse response,
                          @RequestParam(name = "convertHeader", defaultValue = "false") boolean convertHeader) {
        try {
            setResponseHeaders(getFilename("topics"), response);
            exportService.staticTopicExport(response.getOutputStream(), convertHeader);
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
            setResponseHeaders(getFileName(caseType, "topics_teams"), response);
            exportService.staticTopicsWithTeamsExport(response.getOutputStream(), caseType, convertHeader);
        } catch (Exception ex) {
            log.error("Error exporting CSV file for static topic list for reason {}", ex.toString());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @GetMapping("/export/teams")
    public void getTeams(HttpServletResponse response,
                         @RequestParam(name = "convertHeader", defaultValue = "false") boolean convertHeader) {
        try {
            setResponseHeaders(getFilename("teams"), response);
            exportService.staticTeamExport(response.getOutputStream(), convertHeader);
        } catch (Exception ex) {
            log.error("Error exporting CSV file for static team list for reason {}", ex.toString());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @GetMapping("/export/users/teams")
    public void getUsersWithTeams(HttpServletResponse response) {
        try {
            setResponseHeaders(getFilename("users_teams"), response);
            exportService.userWithTeamsExport(response.getOutputStream());

        } catch (Exception ex) {
            log.error("Error exporting CSV file for static team list for reason {}", ex.toString());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @GetMapping("/export/units/teams")
    public void getUnitsForTeams(HttpServletResponse response,
                                 @RequestParam(name = "convertHeader", defaultValue = "false") boolean convertHeader) {
        try {
            setResponseHeaders(getFilename("units_teams"), response);
            exportService.staticUnitsForTeamsExport(response.getOutputStream(), convertHeader);

        } catch (Exception ex) {
            log.error("Error exporting CSV file for static units for teams list for reason {}", ex.toString());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @GetMapping("/export/users")
    public void getUsers(HttpServletResponse response,
                         @RequestParam(name = "convertHeader", defaultValue = "false") boolean convertHeader) {
        try {
            setResponseHeaders(getFilename("users"), response);
            exportService.staticUserExport(response.getOutputStream(), convertHeader);
        } catch (Exception ex) {
            log.error("Error exporting CSV file for static user list for reason {}", ex.toString());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @GetMapping(value = "/export/custom/{code}")
    public @ResponseBody
    void getCustomDataExport(HttpServletResponse response,
                             @PathVariable("code") String code,
                             @RequestParam(name = "convertHeader", defaultValue = "false") boolean convertHeader) {
        try {
            setResponseHeaders(getFilename(code), response);
            customExportService.customExport(response, code, convertHeader);
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

    @PostMapping(value = "/admin/export/custom/{viewName}/refresh")
    public @ResponseBody void refreshMaterialisedView(HttpServletResponse response, @PathVariable("viewName") String viewName) {
        try {
            customExportService.refreshMaterialisedView(viewName);
            response.setStatus(HttpStatus.OK.value());
        } catch (Exception ex) {
            log.error("Error refreshing materialised view {}: {}", viewName, ex.getMessage());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    private static String getFileName(String caseType, ExportType exportType) {
        return String.format("%s-%s-%s.csv", caseType.toLowerCase(), exportType.toString().toLowerCase(), LocalDate.now());
    }

    private static String getFileName(String caseType, String export) {
        return String.format("%s-%s-%s.csv", caseType.toLowerCase(), export, LocalDate.now());
    }

    private static String getFilename(String export) {
        return String.format("%s-%s.csv", export, LocalDate.now());
    }

    private static void setResponseHeaders(String filename, HttpServletResponse response) {
        response.setContentType("text/csv");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename="+ filename);
        response.setStatus(HttpStatus.OK.value());
    }
}
