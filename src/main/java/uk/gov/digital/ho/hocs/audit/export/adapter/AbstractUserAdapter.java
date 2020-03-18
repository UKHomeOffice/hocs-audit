package uk.gov.digital.ho.hocs.audit.export.adapter;

import org.springframework.util.StringUtils;
import uk.gov.digital.ho.hocs.audit.export.infoclient.dto.UserDto;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


public abstract class AbstractUserAdapter implements ExportViewFieldAdapter {

    private final Map<String, UserDto> userMap;

    public AbstractUserAdapter(Set<UserDto> users) {
        userMap = users.stream().collect(Collectors.toMap(userDto -> userDto.getId(), userDto -> userDto));
    }

    @Override
    public String convert(Object input) {
        if (input instanceof String && !StringUtils.isEmpty(input)) {
            UserDto userDto = userMap.get(input);
            return userDto != null ? getUserData(userDto) : null;
        }
        return null;
    }

    protected abstract String getUserData(UserDto userDto);
}
