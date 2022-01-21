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
    private List<String> teamNames;
    private Map<String, String> unitNames;
    private boolean enabled;

    public static UserWithTeamsDto from(UserDto user, List<String> teamNames, Map<String, String> unitNames, boolean enabled) {
        return new UserWithTeamsDto(user.getId(), user.getUsername(), user.getEmail(), user.getFirstName(), user.getLastName(), teamNames, unitNames, enabled);
    }
}