package uk.gov.digital.ho.hocs.audit.client.info.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class SomuTypeDto {

    @JsonProperty("uuid")
    private UUID uuid;

    @JsonProperty("caseType")
    private String caseType;

    @JsonProperty("type")
    private String type;

    @JsonProperty("schema")
    private String schema;

    @JsonProperty("active")
    private boolean active;
}
