package uk.gov.digital.ho.hocs.audit.client.info.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class EntityDto {

  @JsonProperty("id")
  private Long id;

  @JsonProperty("uuid")
  private UUID uuid;

  @JsonProperty("simpleName")
  private String simpleName;

  @JsonProperty("data")
  private EntityDataDto data;

  @JsonProperty("entityListUUID")
  private UUID entityListUUID;

  @JsonProperty("active")
  private boolean active;

}
