package uk.gov.digital.ho.hocs.audit.export.adapter;

import org.springframework.util.StringUtils;
import uk.gov.digital.ho.hocs.audit.export.infoclient.ExportViewConstants;
import uk.gov.digital.ho.hocs.audit.export.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.audit.export.infoclient.dto.TeamDto;
import uk.gov.digital.ho.hocs.audit.export.infoclient.dto.UserDto;


public class TeamAdapter extends AbstractExportViewFieldAdapter {

    public TeamAdapter(InfoClient infoClient) {
        super(infoClient);
    }

    @Override
    public String getAdapterType() {
        return ExportViewConstants.FIELD_ADAPTER_TEAM_NAME;
    }

    @Override
    public String convert(Object input) {
        if (input instanceof String && !StringUtils.isEmpty(input)) {
            TeamDto teamDto = infoClient.getTeam((String) input);
            return teamDto != null ? teamDto.getDisplayName() : null;
        }
        return null;
    }
}
