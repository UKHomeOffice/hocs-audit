package uk.gov.digital.ho.hocs.audit.client.casework.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class GetTopicResponse {

    @JsonProperty("value")
    private UUID uuid;

    @JsonProperty("created")
    private LocalDateTime created;

    @JsonProperty("caseUUID")
    private UUID caseUUID;

    @JsonProperty("label")
    private String topicText;

    @JsonProperty("topicUUID")
    private UUID topicUUID;

}
