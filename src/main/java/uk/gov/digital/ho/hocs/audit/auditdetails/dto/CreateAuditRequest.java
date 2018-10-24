package uk.gov.digital.ho.hocs.audit.auditdetails.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CreateAuditRequest {

    @JsonProperty("correlation_id")
    private String correlationID;

    @JsonProperty("raising_service")
    private String raisingService;

    @JsonProperty("before")
    private String before;

    @JsonProperty("after")
    private String after;

    @JsonProperty("namespace")
    private String namespace;

    @JsonProperty("type")
    private String type;

    @JsonProperty("user_id")
    private String userID;

}