package uk.gov.digital.ho.hocs.audit.client.info.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class EntityDataDto {

    @JsonProperty("title")
    private String title;

}
