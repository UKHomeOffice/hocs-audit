package uk.gov.digital.ho.hocs.audit.export.adapter;

import uk.gov.digital.ho.hocs.audit.export.infoclient.ExportViewConstants;
import uk.gov.digital.ho.hocs.audit.export.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.audit.export.infoclient.dto.UserDto;


public class UsernameAdapter extends AbstractUserAdapter {

    public UsernameAdapter(InfoClient infoClient) {
        super(infoClient);
    }

    @Override
    public String getAdapterType() {
        return ExportViewConstants.FIELD_ADAPTER_USERNAME;
    }

    @Override
    protected String getUserData(UserDto userDto) {
        return userDto.getUsername();
    }

}
