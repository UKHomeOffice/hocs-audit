package uk.gov.digital.ho.hocs.audit.export.adapter;

import uk.gov.digital.ho.hocs.audit.export.infoclient.ExportViewConstants;
import uk.gov.digital.ho.hocs.audit.export.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.audit.export.infoclient.dto.UserDto;


public class UserFirstAndLastNameAdapter extends AbstractUserAdapter {

    public UserFirstAndLastNameAdapter(InfoClient infoClient) {
        super(infoClient);
    }

    @Override
    public String getAdapterType() {
        return ExportViewConstants.FIELD_ADAPTER_FIRST_AND_LAST_NAME;
    }

    @Override
    protected String getUserData(UserDto userDto) {
        return String.format("%s, %s", userDto.getFirstName(), userDto.getLastName());
    }

}
