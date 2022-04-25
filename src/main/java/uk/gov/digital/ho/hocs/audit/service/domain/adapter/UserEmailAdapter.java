package uk.gov.digital.ho.hocs.audit.service.domain.adapter;

import uk.gov.digital.ho.hocs.audit.client.info.ExportViewConstants;
import uk.gov.digital.ho.hocs.audit.client.info.dto.UserDto;

import java.util.Set;


public class UserEmailAdapter extends AbstractUserAdapter {

    public UserEmailAdapter(Set<UserDto> users) {
        super(users);
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
