package uk.gov.digital.ho.hocs.audit.entrypoint;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.digital.ho.hocs.audit.core.exception.EntityPermissionException;
import uk.gov.digital.ho.hocs.audit.service.CustomExportService;

import javax.servlet.http.HttpServletResponse;

@Slf4j
@RestController
@Profile("extracts")
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

        try {
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + getFilename(viewName));
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

    public String getFilename(String viewName) {
        return String.format("%s-%s.csv", viewName, customExportService.getViewLastRefreshedDate(viewName));
    }

}
