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
import uk.gov.digital.ho.hocs.audit.entrypoint.dto.CustomExportFilter;
import uk.gov.digital.ho.hocs.audit.service.CustomExportService;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;

@Slf4j
@RestController
@Profile("extracts")
public class CustomExportResource {

    private final CustomExportService customExportService;

    public CustomExportResource(CustomExportService customExportService) {
        this.customExportService = customExportService;
    }

    @GetMapping(value = "/export/custom/{viewName}", produces = "text/csv;charset=UTF-8")
    public @ResponseBody void getCustomDataExport(
        HttpServletResponse response,
        @PathVariable("viewName") String viewName,
        @RequestParam(name = "convertHeader", defaultValue = "false") boolean convertHeader,
        @RequestParam(name = "filterBy", required = false) String filterBy,
        @RequestParam(name = "dateFrom", required = false) LocalDate dateFrom,
        @RequestParam(name = "dateTo", required = false) LocalDate dateTo,
        @RequestParam(name = "value", required = false) String value,
        @RequestParam(name = "includeEmpty", defaultValue = "false") boolean includeEmpty
    ) throws IOException {

        try {
            CustomExportFilter filter = new CustomExportFilter(filterBy, dateFrom, dateTo, value, includeEmpty);
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + getFilename(viewName));
            customExportService.export(response, viewName, convertHeader, filter);
            response.setStatus(200);
        } catch (Exception ex) {
            log.error("Error exporting CSV file for custom report {}: {}", viewName, ex.getMessage());
            if (ex instanceof HttpClientErrorException) {
                response.setStatus(((HttpClientErrorException) ex).getStatusCode().value());
            } else if (ex instanceof EntityPermissionException) {
                response.setStatus(HttpStatus.FORBIDDEN.value());
            } else if (ex instanceof CustomExportFilter.FilterValidationException) {
                response.setStatus(HttpStatus.BAD_REQUEST.value());
                response.getOutputStream().println(ex.getMessage());
            } else {
                response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            }
        }
    }

    public String getFilename(String viewName) {
        return String.format("%s-%s.csv", viewName, customExportService.getViewLastRefreshedDate(viewName));
    }

}
