package uk.gov.digital.ho.hocs.audit.export.caseworkclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

@AllArgsConstructor
@Getter
public class GetCorrespondentsResponse {

    @JsonProperty("correspondents")
    Set<GetCorrespondentOutlineResponse> correspondents;

    public String getPrimaryCorrespondentName(){
        Optional<GetCorrespondentOutlineResponse> primary = Stream.ofNullable(correspondents)
                .flatMap(Collection::stream)
                .flatMap(Stream::ofNullable)
                .filter(GetCorrespondentOutlineResponse::getIsPrimary)
                .findAny();

        return primary.map(GetCorrespondentOutlineResponse::getFullname).orElse(null);
    }

}
