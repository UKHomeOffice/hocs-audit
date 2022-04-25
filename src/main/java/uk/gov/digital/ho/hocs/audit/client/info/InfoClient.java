package uk.gov.digital.ho.hocs.audit.client.info;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.audit.client.info.dto.CaseTypeActionDto;
import uk.gov.digital.ho.hocs.audit.client.info.dto.CaseTypeDto;
import uk.gov.digital.ho.hocs.audit.client.info.dto.EntityDto;
import uk.gov.digital.ho.hocs.audit.client.info.dto.ExportViewDto;
import uk.gov.digital.ho.hocs.audit.client.info.dto.SomuTypeDto;
import uk.gov.digital.ho.hocs.audit.client.info.dto.TeamDto;
import uk.gov.digital.ho.hocs.audit.client.info.dto.TopicDto;
import uk.gov.digital.ho.hocs.audit.client.info.dto.TopicTeamDto;
import uk.gov.digital.ho.hocs.audit.client.info.dto.UnitDto;
import uk.gov.digital.ho.hocs.audit.client.info.dto.UserDto;
import uk.gov.digital.ho.hocs.audit.core.RestHelper;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class InfoClient {

    private final RestHelper restHelper;
    private final String rootUri;

    @Autowired
    public InfoClient(RestHelper restHelper,
                      @Value("${hocs.info-service}") String infoServiceUri) {
        this.restHelper = restHelper;
        this.rootUri = infoServiceUri;
    }

    @Cacheable(value = "getCaseTypes", unless = "#result == null")
    public Set<CaseTypeDto> getCaseTypes() {
        return restHelper.get(rootUri, "/caseType");
    }

    public Set<UserDto> getUsers() {
        return restHelper.get(rootUri, "/users");
    }

    @Cacheable(value = "getSomuType", unless = "#result == null")
    public SomuTypeDto getSomuType(String caseType, String somuType) {
        return restHelper.get(rootUri, String.format("/somuType/%s/%s", caseType, somuType));
    }

    public Set<TopicDto> getTopics() {
        return restHelper.get(rootUri, "/topics");
    }

    public Set<TopicTeamDto> getTopicsWithTeams(String caseType) {
        return restHelper.get(rootUri, String.format("/topics/%s/teams", caseType));
    }

    public Set<TeamDto> getTeams() {
        return restHelper.get(rootUri, "/team");
    }

    public Set<TeamDto> getAllTeams() {
        return restHelper.get(rootUri, "/team/all");
    }

    public Set<TeamDto> getTeamsForUnit(String unitUUID) {
        return restHelper.get(rootUri, String.format("/unit/%s/teams", unitUUID));
    }

    public TeamDto getTeam(String uuid) {
        return restHelper.get(rootUri, String.format("/team/%s", uuid));
    }

    public UnitDto getUnitByTeam(String uuid) {
        return restHelper.get(rootUri, String.format("/team/%s/unit", uuid));
    }

    public Set<UnitDto> getUnits() {
        return restHelper.get(rootUri, "/unit");
    }

    public LinkedHashSet<String> getCaseExportFields(String caseType) {
        return restHelper.get(rootUri, String.format("/schema/caseType/%s/reporting", caseType));
    }

    public List<ExportViewDto> getExportViews() {
        return restHelper.get(rootUri, "/export");
    }

    public ExportViewDto getExportView(String code) {
        return restHelper.get(rootUri, String.format("/export/%s", code));
    }

    @Cacheable(value = "getEntitiesForList", unless = "#result == null")
    public Set<EntityDto> getEntitiesForList(String simpleName) {
        return restHelper.get(rootUri, String.format("/entity/list/%s", simpleName));
    }

    @Cacheable(value = "getCastTypeActions", unless = "#result == null")
    public List<CaseTypeActionDto> getCaseTypeActions() {
        return restHelper.get(rootUri, "/caseType/actions");
    }

}
