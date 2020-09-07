package uk.gov.digital.ho.hocs.audit.export.caseworkclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class GetCorrespondentOutlinesResponse {

    @JsonProperty("correspondents")
    Set<GetCorrespondentOutlineResponse> correspondents;
}
