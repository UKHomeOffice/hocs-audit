package uk.gov.digital.ho.hocs.audit.auditdetails.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.audit.auditdetails.model.AuditEvent;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public class GetAuditListResponse {

    private List<GetAuditResponse> audits;

    public static GetAuditListResponse from(List<AuditEvent> auditEventSet) {
        List<GetAuditResponse> auditDataResponses = auditEventSet
                .stream()
                .map(GetAuditResponse::from)
                .collect(Collectors.toList());

        return new GetAuditListResponse(auditDataResponses);
    }
}
