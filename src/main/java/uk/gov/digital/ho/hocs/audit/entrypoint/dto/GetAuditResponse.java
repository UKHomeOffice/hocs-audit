package uk.gov.digital.ho.hocs.audit.entrypoint.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.audit.core.utils.LocalDateTimeDeserializer;
import uk.gov.digital.ho.hocs.audit.repository.entity.AuditEvent;

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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS Z", timezone = "UTC")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private ZonedDateTime auditTimestamp;

    @JsonProperty(value = "type")
    private String type;

    @JsonProperty(value = "user_id")
    private String userID;

    public static GetAuditResponse from(AuditEvent auditEvent) {
        return new GetAuditResponse(
                auditEvent.getUuid(),
                auditEvent.getCaseUUID(),
                auditEvent.getStageUUID(),
                auditEvent.getCorrelationID(),
                auditEvent.getRaisingService(),
                auditEvent.getAuditPayload(),
                auditEvent.getNamespace(),
                ZonedDateTime.of(auditEvent.getAuditTimestamp(), ZoneOffset.UTC),
                auditEvent.getType(),
                auditEvent.getUserID());
    }
}
