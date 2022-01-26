package uk.gov.digital.ho.hocs.audit.export.infoclient.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class UserWithTeamsDto {
    private String id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private Map<String, List<String>> unitAndTeamNames;
    private boolean enabled;

    public static UserWithTeamsDto from(UserDto user,  Map<String, List<String>> unitAndTeamNames, boolean enabled) {
        return new UserWithTeamsDto(user.getId(), user.getUsername(), user.getEmail(), user.getFirstName(), user.getLastName(), unitAndTeamNames, enabled);
    }
}