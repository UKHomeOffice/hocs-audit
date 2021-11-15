package uk.gov.digital.ho.hocs.audit.export.caseworkclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class GetCorrespondentOutlineResponse {

    @JsonProperty("uuid")
    private UUID uuid;

    @JsonProperty("fullname")
    private String fullname;

    @JsonProperty("isPrimary")
    @Getter
    private Boolean isPrimary;
}
