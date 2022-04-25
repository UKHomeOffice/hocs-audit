package uk.gov.digital.ho.hocs.audit.entrypoint.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class CreateAuditDto {

    @SerializedName("correlation_id")
    private String correlationID;

    @SerializedName("caseUUID")
    private UUID caseUUID;

    @SerializedName(value= "stageUUID")
    private UUID stageUUID;

    @SerializedName(value= "raising_service")
    private String raisingService;

    @SerializedName(value= "audit_payload")
    private String auditPayload;

    @SerializedName(value= "namespace")
    private String namespace;

    @SerializedName(value="audit_timestamp")
    private LocalDateTime auditTimestamp;

    @SerializedName(value= "type")
    private String type;

    @SerializedName(value= "user_id")
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
