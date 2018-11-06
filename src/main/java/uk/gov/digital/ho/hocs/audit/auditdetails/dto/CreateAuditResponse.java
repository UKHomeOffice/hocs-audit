package uk.gov.digital.ho.hocs.audit.auditdetails.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.audit.auditdetails.model.AuditData;

import java.util.UUID;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class CreateAuditResponse {

    @JsonProperty("uuid")
    private final UUID uuid;

    @JsonProperty("correlation_id")
    private String correlationID;

    @JsonProperty("user_id")
    private String userID;

    public static CreateAuditResponse from(AuditData auditData) {
        return new CreateAuditResponse(auditData.getUuid(), auditData.getCorrelationID(), auditData.getUserID());
    }
}