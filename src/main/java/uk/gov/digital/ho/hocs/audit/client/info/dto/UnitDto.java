package uk.gov.digital.ho.hocs.audit.client.info.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor()
@Getter
@EqualsAndHashCode
public class UnitDto {

    @JsonProperty("displayName")
    private String displayName;

    @JsonProperty("type")
    private String uuid;

    @JsonProperty("shortCode")
    private String shortCode;

}
