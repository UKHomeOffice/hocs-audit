package uk.gov.digital.ho.hocs.audit.entrypoint.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.audit.repository.entity.AuditEvent;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public class GetAuditListResponse {

    private List<GetAuditResponse> audits;

    public static GetAuditListResponse from(List<AuditEvent> auditEvents) {

        var auditResponses = auditEvents.stream().map(GetAuditResponse::from).collect(Collectors.toList());

        return new GetAuditListResponse(auditResponses);
    }

}
