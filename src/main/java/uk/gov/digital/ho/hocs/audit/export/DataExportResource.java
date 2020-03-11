package uk.gov.digital.ho.hocs.audit.export;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.hocs.audit.export.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.audit.export.infoclient.dto.ExportViewDto;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

@Slf4j
@RestController()
public class DataExportResource {

    private ExportService exportService;
    private InfoClient infoClient;

    public DataExportResource(ExportService exportService, InfoClient infoClient) {
        this.exportService = exportService;
        this.infoClient = infoClient;
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

    @GetMapping(value = "/custom/export/{code}", params = {"fromDate", "toDate"})
    public @ResponseBody
    void getCustomDataExport(@RequestParam("fromDate") LocalDate fromDate, @RequestParam("toDate") LocalDate toDate,
                             @PathVariable("code") String code, HttpServletResponse response) {


        ExportViewDto exportViewDto = infoClient.getExportView(code);


        if(exportViewDto != null){
            try {
                response.setContentType("text/csv");
                response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + exportViewDto.getDisplayName() + ".csv" );
                exportService.customExport(fromDate, toDate, response.getOutputStream(), exportViewDto);
                response.setStatus(200);
            } catch (Exception ex) {
                log.error("Error exporting CSV file for custom report {}", code);
                response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            }
        }

    }

    private String getFileName(String caseType, ExportType exportType) {
        return String.format("%s-%s.csv", caseType.toLowerCase(), exportType.toString().toLowerCase());
    }
}
