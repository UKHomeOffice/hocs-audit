package uk.gov.digital.ho.hocs.audit.entrypoint;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.digital.ho.hocs.audit.service.CustomExportService;

import javax.servlet.http.HttpServletResponse;

@Slf4j
@RestController
public class AdminResource {

    private final CustomExportService customExportService;

    public AdminResource(CustomExportService customExportService) {
        this.customExportService = customExportService;
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

}
