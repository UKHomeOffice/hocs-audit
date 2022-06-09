package uk.gov.digital.ho.hocs.audit.entrypoint;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.digital.ho.hocs.audit.core.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.audit.service.CustomExportService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static uk.gov.digital.ho.hocs.audit.core.LogEvent.EXPORT_FAILURE_CUSTOM;
import static uk.gov.digital.ho.hocs.audit.core.utils.FileNameHelper.getFilename;

@Slf4j
@RestController
public class CustomExportResource {

    private final CustomExportService customExportService;

    public CustomExportResource(CustomExportService customExportService) {
        this.customExportService = customExportService;
    }

    @GetMapping(value = "/export/custom/{viewName}", produces = "text/csv;charset=UTF-8")
    public void getCustomDataExport(HttpServletResponse response,
                             @PathVariable("viewName") String viewName,
                             @RequestParam(name = "convertHeader", defaultValue = "false") boolean convertHeader) {
        try {
            setResponseHeaders(response,getFilename(viewName));
            customExportService.export(response, viewName, convertHeader);
        }
        catch (IOException e) {
            throw new ApplicationExceptions.AuditExportException(e, EXPORT_FAILURE_CUSTOM, "Unable to export Custom row for %s", viewName);
        }
        response.setStatus(HttpStatus.OK.value());
    }

    @PostMapping(value = "/admin/export/custom/{viewName}/refresh")
    public void refreshMaterialisedView(HttpServletResponse response, @PathVariable("viewName") String viewName) {
        customExportService.refreshMaterialisedView(viewName);
        response.setStatus(HttpStatus.OK.value());
    }

    private void setResponseHeaders(HttpServletResponse response, String filename) throws IOException {
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=" + filename);
        response.setStatus(HttpStatus.OK.value());
        response.flushBuffer();
    }

}
