package uk.gov.digital.ho.hocs.audit.entrypoint.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class DeleteCaseAuditResponse {

    @JsonProperty("correlation_id")
    private String correlationID;

    @JsonProperty("caseUUID")
    private UUID caseUUID;

    @JsonProperty("deleted")
    private Boolean deleted;

    @JsonProperty("auditCount")
    private Integer auditCount;

    public static DeleteCaseAuditResponse from(UUID caseUUID, DeleteCaseAuditDto request, Integer auditCount) {
        return new DeleteCaseAuditResponse(request.getCorrelationID(), caseUUID, request.getDeleted(), auditCount);
    }

}
