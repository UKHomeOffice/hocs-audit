package uk.gov.digital.ho.hocs.audit.entrypoint;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.digital.ho.hocs.audit.service.StaticTeamService;
import uk.gov.digital.ho.hocs.audit.service.StaticTopicAndTeamService;
import uk.gov.digital.ho.hocs.audit.service.StaticTopicService;
import uk.gov.digital.ho.hocs.audit.service.StaticUnitAndTeamService;
import uk.gov.digital.ho.hocs.audit.service.StaticUserService;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.CSV_EXPORT_COMPLETE;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.CSV_EXPORT_START;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.EVENT;

@Slf4j
@RestController
public class StaticExportResource {

    private final StaticTeamService staticTeamService;
    private final StaticTopicService staticTopicService;
    private final StaticTopicAndTeamService staticTopicAndTeamService;
    private final StaticUserService staticUserService;
    private final StaticUnitAndTeamService staticUnitAndTeamService;

    public StaticExportResource(StaticTeamService staticTeamService,
                                StaticTopicService staticTopicService,
                                StaticTopicAndTeamService staticTopicAndTeamService,
                                StaticUserService staticUserService,
                                StaticUnitAndTeamService staticUnitAndTeamService) {
        this.staticTeamService = staticTeamService;
        this.staticTopicService = staticTopicService;
        this.staticTopicAndTeamService = staticTopicAndTeamService;
        this.staticUserService = staticUserService;
        this.staticUnitAndTeamService = staticUnitAndTeamService;
    }

    @GetMapping(value = "/export/topics", produces = "text/csv")
    public void getTopics(HttpServletResponse response,
                          @RequestParam(name = "convertHeader", defaultValue = "false") boolean convertHeader) {
        try {
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=" + getFilename("topics"));

            log.info("Exporting topics to CSV", value(EVENT, CSV_EXPORT_START));
            staticTopicService.export(response.getOutputStream(), convertHeader);
            log.info("Completed export of topics to CSV", value(EVENT, CSV_EXPORT_COMPLETE));
        } catch (Exception ex) {
            log.error("Error exporting CSV file for static topic list for reason {}", ex.toString());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @GetMapping(value = "/export/topics/{caseType}/teams", produces = "text/csv")
    public void getTopicsWithTeams(HttpServletResponse response,
                                   @PathVariable("caseType") String caseType,
                                   @RequestParam(name = "convertHeader", defaultValue = "false") boolean convertHeader) {
        try {
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=" + getFileName(caseType, "topics_teams"));

            log.info("Exporting topics and teams to CSV", value(EVENT, CSV_EXPORT_START));
            staticTopicAndTeamService.export(response.getOutputStream(), caseType, convertHeader);
            log.info("Completed export of topics and teams to CSV", value(EVENT, CSV_EXPORT_COMPLETE));
        } catch (Exception ex) {
            log.error("Error exporting CSV file for static topic list for reason {}", ex.toString());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @GetMapping(value = "/export/teams", produces = "text/csv")
    public void getTeams(HttpServletResponse response,
                         @RequestParam(name = "convertHeader", defaultValue = "false") boolean convertHeader) {
        try {
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=" + getFilename("teams"));

            log.info("Exporting teams to CSV", value(EVENT, CSV_EXPORT_START));
            staticTeamService.export(response.getOutputStream(), convertHeader);
            log.info("Completed export of teams to CSV", value(EVENT, CSV_EXPORT_COMPLETE));
        } catch (Exception ex) {
            log.error("Error exporting CSV file for static team list for reason {}", ex.toString());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @GetMapping(value = "/export/units/teams", produces = "text/csv")
    public void getUnitsForTeams(HttpServletResponse response,
                                 @RequestParam(name = "convertHeader", defaultValue = "false") boolean convertHeader) {
        try {
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=" + getFilename("units_teams"));

            log.info("Exporting units with teams to CSV", value(EVENT, CSV_EXPORT_START));
            staticUnitAndTeamService.export(response.getOutputStream(), convertHeader);
            log.info("Completed export of units with teams to CSV", value(EVENT, CSV_EXPORT_COMPLETE));
        } catch (Exception ex) {
            log.error("Error exporting CSV file for static units for teams list for reason {}", ex.toString());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @GetMapping(value = "/export/users", produces = "text/csv")
    public void getUsers(HttpServletResponse response,
                         @RequestParam(name = "convertHeader", defaultValue = "false") boolean convertHeader) {
        try {
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=" + getFilename("users"));

            log.info("Exporting users to CSV", value(EVENT, CSV_EXPORT_START));
            staticUserService.export(response.getOutputStream(), convertHeader);
            log.info("Completed export of users to CSV", value(EVENT, CSV_EXPORT_COMPLETE));
        } catch (Exception ex) {
            log.error("Error exporting CSV file for static user list for reason {}", ex.toString());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    private String getFileName(String caseType, String export) {
        return String.format("%s-%s-%s.csv", caseType.toLowerCase(), export, LocalDate.now());
    }

    private String getFilename(String export) {
        return String.format("%s-%s.csv", export, LocalDate.now());
    }
}
