package uk.gov.digital.ho.hocs.audit.auditdetails.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.audit.auditdetails.model.AuditData;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public class GetAuditListSummaryResponse {

    @JsonProperty(value= "audits")
    private List<GetAuditSummaryResponse> audits;

    public static GetAuditListSummaryResponse from(List<AuditData> auditDataSet) {
        List<GetAuditSummaryResponse> auditDataResponses = auditDataSet
                .stream()
                .map(GetAuditSummaryResponse::from)
                .collect(Collectors.toList());

        return new GetAuditListSummaryResponse(auditDataResponses);
    }
}