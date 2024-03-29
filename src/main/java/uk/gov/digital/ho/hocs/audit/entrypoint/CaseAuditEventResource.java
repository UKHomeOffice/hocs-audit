package uk.gov.digital.ho.hocs.audit.entrypoint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.digital.ho.hocs.audit.entrypoint.dto.DeleteCaseAuditDto;
import uk.gov.digital.ho.hocs.audit.entrypoint.dto.DeleteCaseAuditResponse;
import uk.gov.digital.ho.hocs.audit.entrypoint.dto.GetAuditListResponse;
import uk.gov.digital.ho.hocs.audit.service.AuditEventService;

import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Profile("timeline")
@RestController
class CaseAuditEventResource {

    private final AuditEventService auditEventService;

    @Autowired
    public CaseAuditEventResource(AuditEventService auditEventService) {
        this.auditEventService = auditEventService;
    }

    @GetMapping(value = "/audit/case/{caseUUID}", params = { "types" }, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<GetAuditListResponse> getAudits(@PathVariable UUID caseUUID,
                                                          @RequestParam("types") String types) {
        var filterTypes = types.split(",");
        var auditEvents = auditEventService.getAuditDataByCaseUUID(caseUUID, filterTypes);
        return ResponseEntity.ok(GetAuditListResponse.from(auditEvents));
    }

    @GetMapping(value = "/audit/case/{caseUUID}", params = { "types", "fromDate" }, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<GetAuditListResponse> getAudits(@PathVariable UUID caseUUID,
                                                          @RequestParam("types") String types,
                                                          @RequestParam("fromDate") LocalDate fromDate) {
        var filterTypes = types.split(",");
        var auditEvents = auditEventService.getAuditDataByCaseUUID(caseUUID, filterTypes);
        return ResponseEntity.ok(GetAuditListResponse.from(auditEvents));
    }
    @PostMapping(value = "/audit/case/{caseUUID}/delete",
                 consumes = APPLICATION_JSON_VALUE,
                 produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<DeleteCaseAuditResponse> deleteCaseAudit(@PathVariable UUID caseUUID,
                                                                   @RequestBody DeleteCaseAuditDto request) {
        Integer auditCount = auditEventService.deleteCaseAudit(caseUUID, request.getDeleted());
        return ResponseEntity.ok(DeleteCaseAuditResponse.from(caseUUID, request, auditCount));
    }

}
