package uk.gov.digital.ho.hocs.audit.auditdetails.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.audit.auditdetails.model.AuditData;

import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public class GetAuditsResponse {

    @JsonProperty(value= "audits")
    private Set<GetAuditResponse> audits;

    public static GetAuditsResponse from(Set<AuditData> auditDatas) {
        Set<GetAuditResponse> auditDataResponses = auditDatas
                .stream()
                .map(GetAuditResponse::from)
                .collect(Collectors.toSet());

        return new GetAuditsResponse(auditDataResponses);
    }
}