package uk.gov.digital.ho.hocs.audit.entrypoint;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.digital.ho.hocs.audit.core.exception.EntityPermissionException;
import uk.gov.digital.ho.hocs.audit.service.CustomExportService;

import javax.servlet.http.HttpServletResponse;

@Slf4j
@RestController
public class CustomExportResource {

    private final CustomExportService customExportService;

    public CustomExportResource(CustomExportService customExportService) {
        this.customExportService = customExportService;
    }

    @GetMapping(value = "/export/custom/{viewName}", produces = "text/csv;charset=UTF-8")
    public @ResponseBody void getCustomDataExport(HttpServletResponse response,
                                                  @PathVariable("viewName") String viewName,
                                                  @RequestParam(name = "convertHeader", defaultValue = "false")
                                                  boolean convertHeader) {
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + getFilename(viewName));

        try {
            customExportService.export(response, viewName, convertHeader);
            response.setStatus(200);
        } catch (Exception ex) {
            log.error("Error exporting CSV file for custom report {}: {}", viewName, ex.getMessage());
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
    public @ResponseBody void refreshMaterialisedView(HttpServletResponse response,
                                                      @PathVariable("viewName") String viewName) {
        try {
            customExportService.refreshMaterialisedView(viewName);
            response.setStatus(HttpStatus.OK.value());
        } catch (Exception ex) {
            log.error("Error refreshing materialised view {}: {}", viewName, ex.getMessage());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    //customExportService.getViewLastRefreshedDate(viewName) does not work if view empty

    public String getFilename(String viewName) {
        return String.format("%s-%s.csv", viewName, "");
    }

}
