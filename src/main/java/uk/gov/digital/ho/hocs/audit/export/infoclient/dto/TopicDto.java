package uk.gov.digital.ho.hocs.audit.export.infoclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor()
@Getter
public class TopicDto {

    @JsonProperty("label")
    private String label;

    @JsonProperty("value")
    private UUID value;

}
