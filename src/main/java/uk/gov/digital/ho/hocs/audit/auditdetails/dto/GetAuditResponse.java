package uk.gov.digital.ho.hocs.audit.auditdetails.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.audit.application.LocalDateTimeDeserializer;
import uk.gov.digital.ho.hocs.audit.auditdetails.model.AuditData;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@Getter
public class GetAuditResponse {

    @JsonProperty(value = "uuid")
    private UUID uuid;

    @JsonProperty(value = "correlation_id")
    private String correlationID;

    @JsonProperty(value = "raising_service")
    private String raisingService;

    @JsonProperty(value = "audit_payload")
    private String auditPayload;

    @JsonProperty(value = "namespace")
    private String namespace;

    @JsonProperty(value = "audit_timestamp")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime auditTimestamp;

    @JsonProperty(value = "type")
    private String type;

    @JsonProperty(value = "user_id")
    private String userID;

    public static GetAuditResponse from(AuditData auditData) {
        return new GetAuditResponse(
                auditData.getUuid(),
                auditData.getCorrelationID(),
                auditData.getRaisingService(),
                auditData.getAuditPayload(),
                auditData.getNamespace(),
                auditData.getAuditTimestamp(),
                auditData.getType(),
                auditData.getUserID());
    }
}