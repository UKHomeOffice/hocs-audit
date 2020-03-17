package uk.gov.digital.ho.hocs.audit.export.adapter;

import org.springframework.util.StringUtils;
import uk.gov.digital.ho.hocs.audit.export.infoclient.ExportViewConstants;
import uk.gov.digital.ho.hocs.audit.export.infoclient.dto.TeamDto;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


public class TeamNameAdapter implements ExportViewFieldAdapter {


    private Map<String, TeamDto> teamMap;

    public TeamNameAdapter(Set<TeamDto> teams) {
        teamMap = teams.stream().collect(Collectors.toMap(teamDto -> teamDto.getUuid().toString(), teamDto -> teamDto));
    }

    @Override
    public String getAdapterType() {
        return ExportViewConstants.FIELD_ADAPTER_TEAM_NAME;
    }

    @Override
    public String convert(Object input) {
        if (input instanceof String && !StringUtils.isEmpty(input)) {
            TeamDto teamDto = teamMap.get(input);
            return teamDto != null ? teamDto.getDisplayName() : null;
        }
        return null;
    }
}
