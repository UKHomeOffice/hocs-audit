package uk.gov.digital.ho.hocs.audit.client.info.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor()
@Getter
public class FieldDto {

    @JsonProperty("uuid")
    private UUID uuid;

    @JsonProperty("name")
    private String name;

    @JsonProperty("label")
    private String label;

    @JsonProperty("component")
    private String component;

    @JsonRawValue
    private String[] validation;

    @JsonProperty("summary")
    private boolean summary;

    @JsonProperty("export")
    private boolean export;

    @JsonProperty("active")
    private boolean active;
}
