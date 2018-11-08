package uk.gov.digital.ho.hocs.audit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.hocs.audit.auditdetails.dto.*;
import uk.gov.digital.ho.hocs.audit.auditdetails.model.AuditData;

import java.util.Set;
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

    @PostMapping(value = "/audit", consumes = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CreateAuditResponse> createAudit(@RequestBody CreateAuditDto request) {
        AuditData auditData = auditDataService.createAudit(request);
        return ResponseEntity.ok(CreateAuditResponse.from(auditData));
    }

    @GetMapping(value = "/audit/{auditUUID}", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<GetAuditResponse> getAudit(@PathVariable UUID auditUUID) {
        AuditData auditData = auditDataService.getAuditData(auditUUID);
        return ResponseEntity.ok(GetAuditResponse.from(auditData));
    }

    @GetMapping(value = "/audit/{auditUUID}/summary", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<GetAuditSummaryResponse> getAuditSummary(@PathVariable UUID auditUUID) {
        AuditData auditData = auditDataService.getAuditData(auditUUID);
        return ResponseEntity.ok(GetAuditSummaryResponse.from(auditData));
    }

    @GetMapping(value = "/audit/{correlationID}/allaudits", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<GetAuditsResponse> getAuditDataByCorrelationID(@PathVariable String correlationID) {
        Set<AuditData> auditData = auditDataService.getAuditDataByCorrelationID(correlationID);
        return ResponseEntity.ok(GetAuditsResponse.from(auditData));
    }
}