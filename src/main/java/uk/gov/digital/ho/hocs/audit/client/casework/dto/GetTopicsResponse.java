package uk.gov.digital.ho.hocs.audit.client.casework.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class GetTopicsResponse {

    @JsonProperty("topics")
    private Set<GetTopicResponse> topics;

}
