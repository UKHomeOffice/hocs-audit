package uk.gov.digital.ho.hocs.audit.export.caseworkclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class GetCorrespondentWithPrimaryFlagResponse {

    @JsonProperty("fullname")
    @Getter
    private String fullname;

    @JsonProperty("isPrimary")
    @Getter
    private Boolean isPrimary;

}
