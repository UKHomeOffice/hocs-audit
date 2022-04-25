package uk.gov.digital.ho.hocs.audit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.hocs.audit.auditdetails.dto.*;
import uk.gov.digital.ho.hocs.audit.auditdetails.model.AuditEvent;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
class CaseAuditEventResource {
    private final AuditEventService auditEventService;

    @Autowired
    public CaseAuditEventResource(AuditEventService auditEventService) {
        this.auditEventService = auditEventService;
    }

    @GetMapping(value = "/audit/case/{caseUUID}", params = {"types"}, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<GetAuditListResponse> getAudits(@PathVariable UUID caseUUID, @RequestParam("types") String types) {
        List<AuditEvent> auditData = auditEventService.getAuditDataByCaseUUID(caseUUID, types);
        return ResponseEntity.ok(GetAuditListResponse.from(auditData));
    }

    @PostMapping(value = "/audit/case/{caseUUID}/delete", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<DeleteCaseAuditResponse> deleteCaseAudit(@PathVariable UUID caseUUID, @RequestBody DeleteCaseAuditDto request){
        Integer auditCount = auditEventService.deleteCaseAudit(caseUUID, request.getDeleted());
        return ResponseEntity.ok(DeleteCaseAuditResponse.from(caseUUID, request, auditCount));
    }


}
