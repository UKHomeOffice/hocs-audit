package uk.gov.digital.ho.hocs.audit.export.infoclient.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import uk.gov.digital.ho.hocs.audit.export.infoclient.dto.TeamDto;
import uk.gov.digital.ho.hocs.audit.export.infoclient.dto.UserDto;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class UserWithTeamsDto {
    private String id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private List<UUID> teamUUIDs;
    private boolean enabled;

    public static UserWithTeamsDto from(UserDto user, List<UUID> teamUUIDS, boolean enabled) {
        return new UserWithTeamsDto(user.getId(), user.getUsername(), user.getEmail(), user.getFirstName(), user.getLastName(), teamUUIDS, enabled);
    }
}