package uk.gov.digital.ho.hocs.audit.export.infoclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.audit.export.RestHelper;
import uk.gov.digital.ho.hocs.audit.export.infoclient.dto.*;
import uk.gov.digital.ho.hocs.audit.export.infoclient.dto.UserWithTeamsDto;

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
        Set<CaseTypeDto> response = restHelper.get(serviceBaseURL, "/caseType",
                new ParameterizedTypeReference<>() {
        });
        log.info("Got {} case types", response.size(), value(EVENT, INFO_CLIENT_GET_CASE_TYPES_SUCCESS));
        return response;
    }

    public Set<UserDto> getUsers() {
        Set<UserDto> users = restHelper.get(serviceBaseURL, "/users", new ParameterizedTypeReference<>() {
        });
        log.info("Got Users {}", value(EVENT, INFO_CLIENT_GET_USERS_SUCCESS));
        return users;
    }

    @Cacheable(value = "getSomuType", unless = "#result == null")
    public SomuTypeDto getSomuType(String caseType, String somuType) {
        SomuTypeDto somuTypeDto = restHelper.get(serviceBaseURL, String.format("/somuType/%s/%s", caseType, somuType), new ParameterizedTypeReference<>() {
        });
        log.info("Got SomuType {}", somuType, value(EVENT, INFO_CLIENT_GET_SOMUTYPE_SUCCESS));
        return somuTypeDto;
    }

    public Set<TopicDto> getTopics() {
        Set<TopicDto> infoTopic = restHelper.get(serviceBaseURL, String.format("/topics"), new ParameterizedTypeReference<>() {
        });
        log.info("Got Topics", value(EVENT, INFO_CLIENT_GET_TOPIC_SUCCESS));
        return infoTopic;
    }

    public Set<TopicTeamDto> getTopicsWithTeams(String caseType) {
        Set<TopicTeamDto> topicTeams = restHelper.get(serviceBaseURL, String.format("/topics/%s/teams", caseType), new ParameterizedTypeReference<>() {
        });
        log.info("Got {} topics with teams", topicTeams.size(), value(EVENT, INFO_CLIENT_GET_TEAMS_SUCCESS));
        return topicTeams;
    }

    public Set<TeamDto> getTeams() {
        Set<TeamDto> teams = restHelper.get(serviceBaseURL, "/team", new ParameterizedTypeReference<>() {
        });
        log.info("Got {} teams", teams.size(), value(EVENT, INFO_CLIENT_GET_TEAMS_SUCCESS));
        return teams;
    }

    public Set<UserWithTeamsDto> getUsersWithTeams() {
        Set<UserWithTeamsDto> users = restHelper.get(serviceBaseURL, "/team/users", new ParameterizedTypeReference<>() {
        });
        log.info("Got {} users", users.size(), value(EVENT, INFO_CLIENT_GET_USERS_AND_TEAMS_SUCCESS));
        return users;
    }

    public Set<TeamDto> getAllTeams() {
        Set<TeamDto> teams = restHelper.get(serviceBaseURL, "/team/all", new ParameterizedTypeReference<>() {
        });
        log.info("Got {} all teams", teams.size(), value(EVENT, INFO_CLIENT_GET_ALL_TEAMS_SUCCESS));
        return teams;
    }

    public Set<TeamDto> getTeamsForUnit(String unitUUID) {
        Set<TeamDto> teams = restHelper.get(serviceBaseURL, String.format("/unit/%s/teams", unitUUID), new ParameterizedTypeReference<>() {
        });
        log.info("Got {} teams for unit", teams.size(), value(EVENT, INFO_CLIENT_GET_TEAMS_SUCCESS));
        return teams;
    }

    public TeamDto getTeam(String uuid) {
        TeamDto result = restHelper.get(serviceBaseURL, String.format("/team/%s", uuid), new ParameterizedTypeReference<>() {
        });
        log.info("Got teamDto for team uuid {}, event {}", uuid, value(EVENT, INFO_CLIENT_GET_TEAM_SUCCESS));
        return result;
    }

    public UnitDto getUnitByTeam(String uuid) {
        UnitDto result = restHelper.get(serviceBaseURL, String.format("/team/%s/unit", uuid), new ParameterizedTypeReference<>() {
        });
        log.info("Got teamDto for team uuid {}, event {}", uuid, value(EVENT, INFO_CLIENT_GET_TEAM_SUCCESS));
        return result;
    }

    public Set<UnitDto> getUnits() {
        Set<UnitDto> units = restHelper.get(serviceBaseURL, "/unit", new ParameterizedTypeReference<>() {
        });
        log.info("Got {} teams", units.size(), value(EVENT, INFO_CLIENT_GET_UNITS_SUCCESS));
        return units;
    }

    public LinkedHashSet<String> getCaseExportFields(String caseType) {
        LinkedHashSet<String> response = restHelper.get(serviceBaseURL, String.format("/schema/caseType/%s/reporting", caseType), new ParameterizedTypeReference<>() {
        });
        log.info("Got {} case reporting fields for CaseType {}", response.size(), caseType, value(EVENT, INFO_CLIENT_GET_EXPORT_FIELDS_SUCCESS));
        return response;
    }

    public List<ExportViewDto> getExportViews() {
        List<ExportViewDto> views = restHelper.get(serviceBaseURL, "/export", new ParameterizedTypeReference<>() {
        });
        log.info("Got {} export views, event: {}", views.size(), value(EVENT, INFO_CLIENT_GET_EXPORT_VIEWS_SUCCESS));
        return views;
    }

    public ExportViewDto getExportView(String code) {
        ExportViewDto view = restHelper.get(serviceBaseURL, String.format("/export/%s", code), new ParameterizedTypeReference<>() {
        });

        if (view != null) {
            log.info("Got {} export view, event: {}", view.getDisplayName(), value(EVENT, INFO_CLIENT_GET_EXPORT_VIEW_SUCCESS));
            return view;
        }
        log.warn("Could not find export view for '{}', event: {}", code, value(EVENT, INFO_CLIENT_GET_EXPORT_VIEW_FAILURE));
        return null;
    }

    @Cacheable(value = "getEntitiesForList", unless = "#result == null")
    public Set<EntityDto> getEntitiesForList(String simpleName) {
        Set<EntityDto> entities = restHelper.get(serviceBaseURL, String.format("/entity/list/%s", simpleName), new ParameterizedTypeReference<>() {
        });
        log.info("Got {} entities for list", entities.size(), value(EVENT, INFO_CLIENT_GET_TEAMS_SUCCESS));
        return entities;
    }

}
