package uk.gov.digital.ho.hocs.audit.export.adapter;

import org.springframework.util.StringUtils;
import uk.gov.digital.ho.hocs.audit.export.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.audit.export.infoclient.dto.UserDto;


public abstract class AbstractUserAdapter extends AbstractExportViewFieldAdapter {

    public AbstractUserAdapter(InfoClient infoClient) {
        super(infoClient);
    }

    @Override
    public String convert(Object input) {
        if (input instanceof String && !StringUtils.isEmpty(input)) {
            UserDto userDto = infoClient.getUser((String) input);
            return userDto != null ? getUserData(userDto) : null;
        }
        return null;
    }

    protected abstract String getUserData(UserDto userDto);
}
