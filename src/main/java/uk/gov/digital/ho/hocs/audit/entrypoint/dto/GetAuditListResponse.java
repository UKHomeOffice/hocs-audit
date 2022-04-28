package uk.gov.digital.ho.hocs.audit.entrypoint.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class GetAuditListResponse {
    private List<GetAuditResponse> audits;
}
