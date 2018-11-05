package uk.gov.digital.ho.hocs.audit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.hocs.audit.auditdetails.dto.CreateAuditDto;
import uk.gov.digital.ho.hocs.audit.auditdetails.dto.CreateAuditResponse;
import uk.gov.digital.ho.hocs.audit.auditdetails.model.AuditData;

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
}