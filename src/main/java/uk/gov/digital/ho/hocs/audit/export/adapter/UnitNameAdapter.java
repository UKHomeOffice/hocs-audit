package uk.gov.digital.ho.hocs.audit.export.adapter;

import org.springframework.util.StringUtils;
import uk.gov.digital.ho.hocs.audit.export.infoclient.ExportViewConstants;
import uk.gov.digital.ho.hocs.audit.export.infoclient.dto.TeamDto;
import uk.gov.digital.ho.hocs.audit.export.infoclient.dto.UnitDto;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


public class UnitNameAdapter implements ExportViewFieldAdapter {

    private final Map<String, TeamDto> teamMap;
    private final Map<String, UnitDto> unitMap;

    public UnitNameAdapter(Set<TeamDto> teams, Set<UnitDto> units) {
        teamMap = teams.stream().collect(Collectors.toMap(teamDto -> teamDto.getUuid().toString(), teamDto -> teamDto));
        unitMap = units.stream().collect(Collectors.toMap(unitDto -> unitDto.getUuid(), unitDto -> unitDto));
    }

    @Override
    public String getAdapterType() {
        return ExportViewConstants.FIELD_ADAPTER_UNIT_NAME;
    }

    @Override
    public String convert(Object input) {
        if (input instanceof String && !StringUtils.isEmpty(input)) {

            TeamDto teamDto = teamMap.get(input);
            if (teamDto != null && StringUtils.hasText(teamDto.getUnitUUID())) {
                UnitDto unitDto = unitMap.get(teamDto.getUnitUUID());
                return unitDto != null ? unitDto.getDisplayName() : null;
            }
        }
        return null;
    }
}
