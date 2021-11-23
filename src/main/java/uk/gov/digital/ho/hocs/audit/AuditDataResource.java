package uk.gov.digital.ho.hocs.audit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.hocs.audit.auditdetails.dto.*;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@Slf4j
@RestController
class AuditDataResource {
    private final AuditDataService auditDataService;

    @Autowired
    public AuditDataResource(AuditDataService auditDataService) {
        this.auditDataService = auditDataService;
    }

    @PostMapping(value = "/audit/case/{caseUUID}/delete", consumes = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<DeleteCaseAuditResponse> deleteCaseAudit(@PathVariable UUID caseUUID, @RequestBody DeleteCaseAuditDto request){
        Integer auditCount = auditDataService.deleteCaseAudit(caseUUID, request.getDeleted());
        return ResponseEntity.ok(DeleteCaseAuditResponse.from(caseUUID, request, auditCount));
    }

}
