package uk.gov.digital.ho.hocs.audit.export.adapter;

import org.springframework.util.StringUtils;
import uk.gov.digital.ho.hocs.audit.export.infoclient.ExportViewConstants;
import uk.gov.digital.ho.hocs.audit.export.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.audit.export.infoclient.dto.UserDto;


public class UserEmailAdapter extends AbstractExportViewFieldAdapter {

    public UserEmailAdapter(InfoClient infoClient) {
        super(infoClient);
    }

    @Override
    public String getAdapterType() {
        return ExportViewConstants.FIELD_ADAPTER_USER_EMAIL;
    }

    @Override
    public String convert(Object input) {
        if (input instanceof String && !StringUtils.isEmpty(input)) {
            UserDto userDto = infoClient.getUser((String) input);
            return userDto != null ? userDto.getEmail() : null;
        }
        return null;
    }
}
