package uk.gov.digital.ho.hocs.audit.auditdetails.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.audit.application.LocalDateTimeDeserializer;
import uk.gov.digital.ho.hocs.audit.auditdetails.model.AuditEvent;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@Getter
public class GetAuditSummaryResponse {

    @JsonProperty(value = "uuid")
    private UUID uuid;

    @JsonProperty(value = "correlation_id", required = true)
    private String correlationID;

    @JsonProperty(value = "raising_service", required = true)
    private String raisingService;




    @JsonProperty(value = "namespace", required = true)
    private String namespace;

    @JsonProperty(value = "audit_timestamp", required = true)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime auditTimestamp;

    @JsonProperty(value = "type", required = true)
    private String type;

    @JsonProperty(value = "user_id", required = true)
    private String userID;

    public static GetAuditSummaryResponse from(AuditEvent auditEvent) {
        return new GetAuditSummaryResponse(
                auditEvent.getUuid(),
                auditEvent.getCorrelationID(),
                auditEvent.getRaisingService(),
                auditEvent.getNamespace(),
                auditEvent.getAuditTimestamp(),
                auditEvent.getType(),
                auditEvent.getUserID());
    }
}
