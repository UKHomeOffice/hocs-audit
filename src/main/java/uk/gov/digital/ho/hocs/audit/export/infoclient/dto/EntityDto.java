package uk.gov.digital.ho.hocs.audit.export.infoclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

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
