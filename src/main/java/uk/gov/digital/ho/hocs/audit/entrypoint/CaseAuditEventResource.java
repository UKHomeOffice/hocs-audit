package uk.gov.digital.ho.hocs.audit.entrypoint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.digital.ho.hocs.audit.entrypoint.dto.GetAuditListResponse;
import uk.gov.digital.ho.hocs.audit.service.AuditEventService;

import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
class CaseAuditEventResource {
    private final AuditEventService auditEventService;

    @Autowired
    public CaseAuditEventResource(AuditEventService auditEventService) {
        this.auditEventService = auditEventService;
    }

    @GetMapping(value = "/audit/case/{caseUUID}", params = {"types"}, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<GetAuditListResponse> getAudits(@PathVariable UUID caseUUID, @RequestParam("types") String types) {
        var filterTypes = types.split(",");
        var auditEvents = auditEventService.getAuditDataByCaseUUID(caseUUID, filterTypes);
        return ResponseEntity.ok(GetAuditListResponse.from(auditEvents));
    }

    @GetMapping(value = "/audit/case/{caseUUID}", params = {"types", "fromDate"}, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<GetAuditListResponse> getAudits(@PathVariable UUID caseUUID, @RequestParam("types") String types, @RequestParam("fromDate") LocalDate fromDate) {
        var filterTypes = types.split(",");
        var auditEvents = auditEventService.getAuditDataByCaseUUID(caseUUID, filterTypes);
        return ResponseEntity.ok(GetAuditListResponse.from(auditEvents));
    }

    @DeleteMapping(value = "/audit/case/{caseUUID}")
    public ResponseEntity<Void> deleteCaseAudit(@PathVariable UUID caseUUID) {
        auditEventService.deleteCaseAudit(caseUUID);
        return ResponseEntity.ok().build();
    }

}
