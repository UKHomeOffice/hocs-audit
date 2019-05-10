package uk.gov.digital.ho.hocs.audit.auditdetails.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.audit.application.LocalDateTimeDeserializer;
import uk.gov.digital.ho.hocs.audit.auditdetails.model.AuditData;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.UUID;

@AllArgsConstructor
@Getter
public class GetAuditResponse {

    @JsonProperty(value = "uuid")
    private UUID uuid;

    @JsonProperty(value = "caseUUID")
    private UUID caseUUID;

    @JsonProperty(value = "stageUUID")
    private UUID stageUUID;

    @JsonProperty(value = "correlation_id")
    private String correlationID;

    @JsonProperty(value = "raising_service")
    private String raisingService;

    @JsonProperty(value = "audit_payload")
    private String auditPayload;

    @JsonProperty(value = "namespace")
    private String namespace;

    @JsonProperty(value = "audit_timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss Z", timezone = "UTC")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private ZonedDateTime auditTimestamp;

    @JsonProperty(value = "type")
    private String type;

    @JsonProperty(value = "user_id")
    private String userID;

    public static GetAuditResponse from(AuditData auditData) {
        return new GetAuditResponse(
                auditData.getUuid(),
                auditData.getCaseUUID(),
                auditData.getStageUUID(),
                auditData.getCorrelationID(),
                auditData.getRaisingService(),
                auditData.getAuditPayload(),
                auditData.getNamespace(),
                ZonedDateTime.of(auditData.getAuditTimestamp(), ZoneOffset.UTC),
                auditData.getType(),
                auditData.getUserID());
    }
}