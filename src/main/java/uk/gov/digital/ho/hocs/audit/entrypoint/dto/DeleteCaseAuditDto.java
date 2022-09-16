package uk.gov.digital.ho.hocs.audit.entrypoint.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DeleteCaseAuditDto {

    @JsonProperty(value = "correlation_id", required = true)
    private String correlationID;

    @JsonProperty(value = "deleted")
    private Boolean deleted;

}
