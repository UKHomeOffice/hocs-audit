package uk.gov.digital.ho.hocs.audit.export.infoclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.audit.export.RestHelper;
import uk.gov.digital.ho.hocs.audit.export.infoclient.dto.*;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.audit.application.LogEvent.*;

@Slf4j
@Component
public class InfoClient {

    private final RestHelper restHelper;
    private final String serviceBaseURL;

    @Autowired
    public InfoClient(RestHelper restHelper,
                      @Value("${hocs.info-service}") String infoService) {
        this.restHelper = restHelper;
        this.serviceBaseURL = infoService;
    }

    @Cacheable(value = "getCaseTypes", unless = "#result == null")
    public Set<CaseTypeDto> getCaseTypes() {
        Set<CaseTypeDto> response = restHelper.get(serviceBaseURL, "/caseType", new ParameterizedTypeReference<Set<CaseTypeDto>>() {
        });
        log.info("Got {} case types", response.size(), value(EVENT, INFO_CLIENT_GET_CASE_TYPES_SUCCESS));
        return response;
    }

    @Cacheable(value = "getUsers", unless = "#result == null")
    public Set<UserDto> getUsers() {
        Set<UserDto> users = restHelper.get(serviceBaseURL, "/users", new ParameterizedTypeReference<Set<UserDto>>() {
        });
        log.info("Got Users {}", value(EVENT, INFO_CLIENT_GET_USERS_SUCCESS));
        return users;
    }

    @Cacheable(value = "getUser", unless = "#result == null")
    public UserDto getUser(String uuid) {
        UserDto result = restHelper.get(serviceBaseURL, String.format("/user/%s", uuid), new ParameterizedTypeReference<UserDto>() {
        });
        log.info("Got userDto for user uuid {}, event {}", uuid, value(EVENT, INFO_CLIENT_GET_USER_SUCCESS));
        return result;
    }

    @Cacheable(value = "getTopics", unless = "#result == null")
    public Set<TopicDto> getTopics() {
        Set<TopicDto> infoTopic = restHelper.get(serviceBaseURL, String.format("/topics"), new ParameterizedTypeReference<Set<TopicDto>>() {
        });
        log.info("Got Topics", value(EVENT, INFO_CLIENT_GET_TOPIC_SUCCESS));
        return infoTopic;
    }

    @Cacheable(value = "getTeams", unless = "#result == null")
    public Set<TeamDto> getTeams() {
        Set<TeamDto> teams = restHelper.get(serviceBaseURL, "/team", new ParameterizedTypeReference<Set<TeamDto>>() {
        });
        log.info("Got {} teams", teams.size(), value(EVENT, INFO_CLIENT_GET_TEAMS_SUCCESS));
        return teams;
    }

    @Cacheable(value = "getTeamsForUnit", unless = "#result == null")
    public Set<TeamDto> getTeamsForUnit(String unitUUID) {
        Set<TeamDto> teams = restHelper.get(serviceBaseURL, String.format("/unit/%s/teams", unitUUID), new ParameterizedTypeReference<Set<TeamDto>>() {
        });
        log.info("Got {} teams for unit", teams.size(), value(EVENT, INFO_CLIENT_GET_TEAMS_SUCCESS));
        return teams;
    }

    @Cacheable(value = "getTeam", unless = "#result == null")
    public TeamDto getTeam(String uuid) {
        TeamDto result = restHelper.get(serviceBaseURL, String.format("/team/%s", uuid), new ParameterizedTypeReference<TeamDto>() {
        });
        log.info("Got teamDto for team uuid {}, event {}", uuid, value(EVENT, INFO_CLIENT_GET_TEAM_SUCCESS));
        return result;
    }

    @Cacheable(value = "getUnitByTeam", unless = "#result == null")
    public UnitDto getUnitByTeam(String uuid) {
        UnitDto result = restHelper.get(serviceBaseURL, String.format("/team/%s/unit", uuid), new ParameterizedTypeReference<UnitDto>() {
        });
        log.info("Got teamDto for team uuid {}, event {}", uuid, value(EVENT, INFO_CLIENT_GET_TEAM_SUCCESS));
        return result;
    }

    @Cacheable(value = "getUnits", unless = "#result == null")
    public Set<UnitDto> getUnits() {
        Set<UnitDto> units = restHelper.get(serviceBaseURL, "/unit", new ParameterizedTypeReference<Set<UnitDto>>() {
        });
        log.info("Got {} teams", units.size(), value(EVENT, INFO_CLIENT_GET_UNITS_SUCCESS));
        return units;
    }

    public LinkedHashSet<String> getCaseExportFields(String caseType) {
        LinkedHashSet<String> response = restHelper.get(serviceBaseURL, String.format("/schema/caseType/%s/reporting", caseType), new ParameterizedTypeReference<LinkedHashSet<String>>() {
        });
        log.info("Got {} case reporting fields for CaseType {}", response.size(), caseType, value(EVENT, INFO_CLIENT_GET_EXPORT_FIELDS_SUCCESS));
        return response;
    }

    public List<ExportViewDto> getExportViews() {
        List<ExportViewDto> views = restHelper.get(serviceBaseURL, "/export", new ParameterizedTypeReference<List<ExportViewDto>>() {
        });
        log.info("Got {} export views, event: {}", views.size(), value(EVENT, INFO_CLIENT_GET_EXPORT_VIEWS_SUCCESS));
        return views;
    }

    public ExportViewDto getExportView(String code) {
        ExportViewDto view = restHelper.get(serviceBaseURL, String.format("/export/%s", code), new ParameterizedTypeReference<ExportViewDto>() {
        });

        if (view != null) {
            log.info("Got {} export view, event: {}", view.getDisplayName(), value(EVENT, INFO_CLIENT_GET_EXPORT_VIEW_SUCCESS));
            return view;
        }
        log.warn("Could not find export view for '{}', event: {}", code, value(EVENT, INFO_CLIENT_GET_EXPORT_VIEW_FAILURE));
        return null;
    }


}