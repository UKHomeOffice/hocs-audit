package uk.gov.digital.ho.hocs.audit.export.infoclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;
import java.util.UUID;

@AllArgsConstructor()
@Getter
public class TopicTeamDto {

    @JsonProperty("label")
    private String displayName;

    @JsonProperty("value")
    private UUID uuid;

    @JsonProperty("teams")
    private Set<TeamDto> teams;
}
