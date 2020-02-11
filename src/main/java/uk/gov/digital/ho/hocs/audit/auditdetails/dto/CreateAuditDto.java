package uk.gov.digital.ho.hocs.audit.auditdetails.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.ho.hocs.audit.application.LocalDateTimeDeserializer;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class CreateAuditDto {

    @JsonProperty(value= "correlation_id", required = true)
    private String correlationID;

    @JsonProperty(value= "caseUUID")
    private UUID caseUUID;

    @JsonProperty(value= "stageUUID")
    private UUID stageUUID;

    @JsonProperty(value= "raising_service", required = true)
    private String raisingService;

    @JsonProperty(value= "audit_payload")
    private String auditPayload;

    @JsonProperty(value= "namespace", required = true)
    private String namespace;

    @JsonProperty(value="audit_timestamp", required = true)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime auditTimestamp;

    @JsonProperty(value= "type", required = true)
    private String type;

    @JsonProperty(value= "user_id", required = true)
    private String userID;

    public CreateAuditDto(String correlationID, String raisingService, String auditPayload, String namespace, LocalDateTime auditTimestamp, String type, String userID){
        this.correlationID = correlationID;
        this.raisingService = raisingService;
        this.auditPayload = auditPayload;
        this.namespace = namespace;
        this.auditTimestamp = auditTimestamp;
        this.type = type;
        this.userID = userID;
    }

    public CreateAuditDto(UUID caseUUID, UUID stageUUID, String correlationID, String raisingService, String auditPayload, String namespace, LocalDateTime auditTimestamp, String type, String userID){
        this(correlationID, raisingService, auditPayload, namespace, auditTimestamp, type, userID);
        this.caseUUID = caseUUID;
        this.stageUUID = stageUUID;
    }

}

