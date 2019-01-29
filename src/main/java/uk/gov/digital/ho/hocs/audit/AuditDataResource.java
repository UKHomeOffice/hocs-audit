package uk.gov.digital.ho.hocs.audit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.hocs.audit.auditdetails.dto.*;
import uk.gov.digital.ho.hocs.audit.auditdetails.model.AuditData;

import java.util.List;
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
        AuditData auditData = auditDataService.createAudit(
                request.getCaseUUID(),
                request.getStageUUID(),
                request.getCorrelationID(),
                request.getRaisingService(),
                request.getAuditPayload(),
                request.getNamespace(),
                request.getAuditTimestamp(),
                request.getType(),
                request.getUserID());
        return ResponseEntity.ok(CreateAuditResponse.from(auditData));
    }

    @GetMapping(value = "/audits", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<GetAuditListResponse> getAudits(@RequestParam("page") int page, @RequestParam int limit) {
        List<AuditData> auditData = auditDataService.getAuditDataList(page, limit);
        return ResponseEntity.ok(GetAuditListResponse.from(auditData));
    }

    @GetMapping(value = "/audits", params = {"fromDate", "toDate"}, produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<GetAuditListResponse> getAuditDataByDateRange(@RequestParam("fromDate") String fromDate, @RequestParam("toDate") String toDate, @RequestParam("page") int page, @RequestParam int limit){
        List<AuditData> auditData = auditDataService.getAuditDataByDateRange(fromDate, toDate, page, limit);
        return ResponseEntity.ok(GetAuditListResponse.from(auditData));
    }

    @GetMapping(value = "/audits/summary", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<GetAuditListSummaryResponse> getAuditsSummary(@RequestParam("page") int page, @RequestParam int limit) {
        List<AuditData> auditData = auditDataService.getAuditDataList(page, limit);
        return ResponseEntity.ok(GetAuditListSummaryResponse.from(auditData));
    }

    @GetMapping(value = "/audits/summary", params = {"fromDate", "toDate"}, produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<GetAuditListSummaryResponse> getAuditDataSummaryByDateRange(@RequestParam("fromDate") String fromDate, @RequestParam("toDate") String toDate, @RequestParam("page") int page, @RequestParam int limit){
        List<AuditData> auditData = auditDataService.getAuditDataByDateRange(fromDate, toDate, page, limit);
        return ResponseEntity.ok(GetAuditListSummaryResponse.from(auditData));
    }

    @GetMapping(value = "/audit/{auditUUID}", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<GetAuditResponse> getAudit(@PathVariable UUID auditUUID) {
        AuditData auditData = auditDataService.getAuditDataByUUID(auditUUID);
        return ResponseEntity.ok(GetAuditResponse.from(auditData));
    }

    @GetMapping(value = "/audit/{auditUUID}/summary", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<GetAuditSummaryResponse> getAuditSummary(@PathVariable UUID auditUUID) {
        AuditData auditData = auditDataService.getAuditDataByUUID(auditUUID);
        return ResponseEntity.ok(GetAuditSummaryResponse.from(auditData));
    }

    @GetMapping(value = "/audit/correlation/{correlationID}", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<GetAuditListResponse> getAuditDataByCorrelationID(@PathVariable String correlationID, @RequestParam("page") int page, @RequestParam int limit) {
        List<AuditData> auditData = auditDataService.getAuditDataByCorrelationID(correlationID, page, limit);
        return ResponseEntity.ok(GetAuditListResponse.from(auditData));
    }

    @GetMapping(value = "/audit/correlation/{correlationID}/summary", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<GetAuditListSummaryResponse> getAuditDataSummaryByCorrelationID(@PathVariable String correlationID, @RequestParam("page") int page, @RequestParam int limit) {
        List<AuditData> auditData = auditDataService.getAuditDataByCorrelationID(correlationID, page, limit);
        return ResponseEntity.ok(GetAuditListSummaryResponse.from(auditData));
    }

    @GetMapping(value = "/audit/username/{userID}", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<GetAuditListResponse> getAuditDataByUserID(@PathVariable String userID, @RequestParam("page") int page, @RequestParam int limit) {
        List<AuditData> auditData = auditDataService.getAuditDataByUserID(userID, page, limit);
        return ResponseEntity.ok(GetAuditListResponse.from(auditData));
    }

    @GetMapping(value = "/audit/username/{userID}", params = {"fromDate", "toDate"}, produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<GetAuditListResponse> getAuditDataByUserIDAndDateRange(@PathVariable String userID, @RequestParam("fromDate") String fromDate, @RequestParam("toDate") String toDate,
                                                                                 @RequestParam("page") int page, @RequestParam int limit){
        List<AuditData> auditData = auditDataService.getAuditDataByUserIDByDateRange(userID, fromDate, toDate, page, limit);
        return ResponseEntity.ok(GetAuditListResponse.from(auditData));
    }

    @GetMapping(value = "/audit/username/{userID}/summary", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<GetAuditListSummaryResponse> getAuditDataSummaryByUserID(@PathVariable String userID, @RequestParam("page") int page, @RequestParam int limit) {
        List<AuditData> auditData = auditDataService.getAuditDataByUserID(userID, page, limit);
        return ResponseEntity.ok(GetAuditListSummaryResponse.from(auditData));
    }

    @GetMapping(value = "/audit/username/{userID}/summary", params = {"fromDate", "toDate"}, produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<GetAuditListSummaryResponse> getAuditDataSummaryByUserIDAndDateRange(@PathVariable String userID, @RequestParam("fromDate") String fromDate, @RequestParam("toDate") String toDate,
                                                                                               @RequestParam("page") int page, @RequestParam int limit){
        List<AuditData> auditData = auditDataService.getAuditDataByUserIDByDateRange(userID, fromDate, toDate, page, limit);
        return ResponseEntity.ok(GetAuditListSummaryResponse.from(auditData));
    }
}