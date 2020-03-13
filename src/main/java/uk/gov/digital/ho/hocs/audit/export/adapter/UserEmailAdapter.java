package uk.gov.digital.ho.hocs.audit.export.adapter;

import uk.gov.digital.ho.hocs.audit.export.infoclient.ExportViewConstants;
import uk.gov.digital.ho.hocs.audit.export.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.audit.export.infoclient.dto.UserDto;


public class UserEmailAdapter extends AbstractUserAdapter {

    public UserEmailAdapter(InfoClient infoClient) {
        super(infoClient);
    }

    @Override
    public String getAdapterType() {
        return ExportViewConstants.FIELD_ADAPTER_USER_EMAIL;
    }

    @Override
    protected String getUserData(UserDto userDto) {
        return userDto.getEmail();
    }

}
