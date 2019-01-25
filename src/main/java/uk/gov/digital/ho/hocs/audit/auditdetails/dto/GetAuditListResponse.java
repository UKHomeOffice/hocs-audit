package uk.gov.digital.ho.hocs.audit.auditdetails.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.audit.auditdetails.model.AuditData;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public class GetAuditListResponse {

    private List<GetAuditResponse> audits;

    public static GetAuditListResponse from(List<AuditData> auditDataSet) {
        List<GetAuditResponse> auditDataResponses = auditDataSet
                .stream()
                .map(GetAuditResponse::from)
                .collect(Collectors.toList());

        return new GetAuditListResponse(auditDataResponses);
    }
}