package uk.gov.digital.ho.hocs.audit.client.casework.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class GetCaseReferenceResponse {

    @JsonProperty("uuid")
    private UUID uuid;

    @JsonProperty("reference")
    private String reference;

}
