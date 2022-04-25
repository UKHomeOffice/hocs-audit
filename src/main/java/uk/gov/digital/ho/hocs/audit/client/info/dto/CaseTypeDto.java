package uk.gov.digital.ho.hocs.audit.client.info.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CaseTypeDto {

    @JsonProperty("displayName")
    private String displayName;

    @JsonProperty("shortCode")
    private String shortCode;

    @JsonProperty("type")
    private String type;

}
