package uk.gov.digital.ho.hocs.audit.entrypoint;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.digital.ho.hocs.audit.core.exception.AuditExportException;
import uk.gov.digital.ho.hocs.audit.service.StaticTeamService;
import uk.gov.digital.ho.hocs.audit.service.StaticTopicAndTeamService;
import uk.gov.digital.ho.hocs.audit.service.StaticTopicService;
import uk.gov.digital.ho.hocs.audit.service.StaticUnitAndTeamService;
import uk.gov.digital.ho.hocs.audit.service.StaticUserService;

import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import static uk.gov.digital.ho.hocs.audit.core.LogEvent.EXPORT_FAILURE_TEAM;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.EXPORT_FAILURE_TOPIC;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.EXPORT_FAILURE_TOPIC_TEAM;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.EXPORT_FAILURE_UNIT_TEAM;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.EXPORT_FAILURE_USER;
import static uk.gov.digital.ho.hocs.audit.core.utils.FileNameHelper.getFileName;
import static uk.gov.digital.ho.hocs.audit.core.utils.FileNameHelper.getFilename;

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

    @GetMapping(value = "/export/topics", produces = "text/csv;charset=UTF-8")
    public void getTopics(HttpServletResponse response,
                          @RequestParam(name = "convertHeader", defaultValue = "false") boolean convertHeader) {
        try {
            setResponseHeaders(response, getFilename("topics"));
            staticTopicService.export(response.getOutputStream(), convertHeader);
        } catch (IOException e) {
            throw new AuditExportException(e, EXPORT_FAILURE_TOPIC, "Unable to export Topics");
        }
    }

    @GetMapping(value = "/export/topics/{caseType}/teams", produces = "text/csv;charset=UTF-8")
    public void getTopicsWithTeams(HttpServletResponse response,
                                   @PathVariable("caseType") String caseType,
                                   @RequestParam(name = "convertHeader", defaultValue = "false") boolean convertHeader) {
        try {
            setResponseHeaders(response, getFileName(caseType, "topics_teams"));
            staticTopicAndTeamService.export(response.getOutputStream(), caseType, convertHeader);
        } catch (IOException e) {
            throw new AuditExportException(e, EXPORT_FAILURE_TOPIC_TEAM, "Unable to export Units and Teams");
        }
    }

    @GetMapping(value = "/export/teams", produces = "text/csv;charset=UTF-8")
    public void getTeams(HttpServletResponse response,
                         @RequestParam(name = "convertHeader", defaultValue = "false") boolean convertHeader) {
        try {
            setResponseHeaders(response, getFilename("teams"));
            staticTeamService.export(response.getOutputStream(), convertHeader);
        } catch (IOException e) {
            throw new AuditExportException(e, EXPORT_FAILURE_TEAM, "Unable to export Teams");
        }
    }

    @GetMapping(value = "/export/units/teams", produces = "text/csv;charset=UTF-8")
    public void getUnitsForTeams(HttpServletResponse response,
                                 @RequestParam(name = "convertHeader", defaultValue = "false") boolean convertHeader) {
        try {
            setResponseHeaders(response, getFilename("units_teams"));
            staticUnitAndTeamService.export(response.getOutputStream(), convertHeader);
        } catch (IOException e) {
            throw new AuditExportException(e, EXPORT_FAILURE_UNIT_TEAM, "Unable to export Units and Teams");
        }
    }

    @GetMapping(value = "/export/users", produces = "text/csv;charset=UTF-8")
    public void getUsers(HttpServletResponse response,
                         @RequestParam(name = "convertHeader", defaultValue = "false") boolean convertHeader) {
        try {
            setResponseHeaders(response, getFilename("users"));
            staticUserService.export(response.getOutputStream(), convertHeader);
        } catch (IOException e) {
            throw new AuditExportException(e, EXPORT_FAILURE_USER, "Unable to export Users");
        }
    }

    private void setResponseHeaders(HttpServletResponse response, String filename) throws IOException {
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=" + filename);
        response.setStatus(HttpStatus.OK.value());
        response.flushBuffer();
    }
}
