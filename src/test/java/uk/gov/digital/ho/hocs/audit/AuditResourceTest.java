package uk.gov.digital.ho.hocs.audit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.hocs.audit.auditdetails.dto.*;
import uk.gov.digital.ho.hocs.audit.auditdetails.model.AuditData;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AuditResourceTest {

    @Mock
    private AuditDataService auditService;

    private String correlationID = "correlationID1";
    private String raisingService = "raisingServiceName";
    private String auditPayload = "";
    private String namespace = "namespaceEventOccurredIn";
    private String type = "eventAuditType";
    private String userID = "userXYZ";
    private LocalDateTime auditTimestamp = LocalDateTime.now();
    private UUID caseUUID = UUID.randomUUID();

    private AuditData validAudit = new AuditData(correlationID, raisingService, auditPayload, namespace, auditTimestamp, type, userID);
    private AuditDataResource auditResource;

    private CreateAuditDto request = new CreateAuditDto(correlationID, raisingService, auditPayload, namespace, auditTimestamp, type, userID);
    private CreateAuditDto requestWithCaseUUID = new CreateAuditDto(caseUUID, correlationID, raisingService, auditPayload, namespace, auditTimestamp, type, userID);


    private String toDate = "2018-11-02";
    private String fromDate = "2018-08-02";
    private int page = 5;
    private int limit = 10;

    @Before
    public void setUp() {
        auditResource = new AuditDataResource(auditService);
    }

    @Test
    public void shouldCreateAuditWithValidParams() {

        when(auditService.createAudit(null, correlationID,
                raisingService,
                auditPayload,
                namespace,
                auditTimestamp,
                type,
                userID)).thenReturn(validAudit);

        ResponseEntity response = auditResource.createAudit(request);

        verify(auditService, times(1)).createAudit(null, correlationID,
                raisingService,
                auditPayload,
                namespace,
                auditTimestamp,
                type,
                userID);

        verifyNoMoreInteractions(auditService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldCreateAuditWithValidParamsCaseUUID() {

        when(auditService.createAudit(caseUUID,
                correlationID,
                raisingService,
                auditPayload,
                namespace,
                auditTimestamp,
                type,
                userID)).thenReturn(validAudit);

        ResponseEntity response = auditResource.createAudit(requestWithCaseUUID);

        verify(auditService, times(1)).createAudit(caseUUID,
                correlationID,
                raisingService,
                auditPayload,
                namespace,
                auditTimestamp,
                type,
                userID);

        verifyNoMoreInteractions(auditService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldReturnAllAuditsFromDefaultDateRange() {
        when(auditService.getAuditDataList(page, limit)).thenReturn(new ArrayList<>());

        ResponseEntity<GetAuditListResponse> response = auditResource.getAudits(page, limit);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(auditService, times(1)).getAuditDataList(page, limit);
        verifyNoMoreInteractions(auditService);
    }

    @Test
    public void shouldReturnAllAuditsFromDateRange() {
        when(auditService.getAuditDataByDateRange(fromDate,toDate, page, limit)).thenReturn(new ArrayList<>());

        ResponseEntity<GetAuditListResponse> response = auditResource.getAuditDataByDateRange(fromDate,toDate, page, limit);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(auditService, times(1)).getAuditDataByDateRange(fromDate,toDate, page, limit);
        verifyNoMoreInteractions(auditService);
    }

    @Test
    public void shouldReturnAllAuditSummariesFromDefaultDateRange() {
        when(auditService.getAuditDataList(page, limit)).thenReturn(new ArrayList<>());

        ResponseEntity<GetAuditListSummaryResponse> response = auditResource.getAuditsSummary(page, limit);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(auditService, times(1)).getAuditDataList(page, limit);
        verifyNoMoreInteractions(auditService);
    }

    @Test
    public void shouldReturnAllAuditSummariesFromDateRange() {
        when(auditService.getAuditDataByDateRange(fromDate,toDate, page, limit)).thenReturn(new ArrayList<>());

        ResponseEntity<GetAuditListSummaryResponse> response = auditResource.getAuditDataSummaryByDateRange(fromDate,toDate, page, limit);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(auditService, times(1)).getAuditDataByDateRange(fromDate,toDate, page, limit);
        verifyNoMoreInteractions(auditService);
    }

    @Test
    public void shouldReturnAuditForUUID() {
        when(auditService.getAuditDataByUUID(any())).thenReturn(validAudit);

        ResponseEntity<GetAuditResponse> response = auditResource.getAudit(any());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(auditService, times(1)).getAuditDataByUUID(any());
        verifyNoMoreInteractions(auditService);
    }

    @Test
    public void shouldReturnAuditSummaryForUUID() {
        when(auditService.getAuditDataByUUID(any())).thenReturn(validAudit);

        ResponseEntity<GetAuditSummaryResponse> response = auditResource.getAuditSummary(any());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(auditService, times(1)).getAuditDataByUUID(any());
        verifyNoMoreInteractions(auditService);
    }

    @Test
    public void shouldReturnAuditsFromCorrelationIDAndFromDefaultDateRange() {
        when(auditService.getAuditDataByCorrelationID(correlationID, page, limit)).thenReturn(new ArrayList<>());

        ResponseEntity<GetAuditListResponse> response = auditResource.getAuditDataByCorrelationID(correlationID, page, limit);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(auditService, times(1)).getAuditDataByCorrelationID(correlationID, page, limit);
        verifyNoMoreInteractions(auditService);
    }

    @Test
    public void shouldReturnAuditSummariesFromCorrelationIDAndDefaultDateRange() {
        when(auditService.getAuditDataByCorrelationID(correlationID, page, limit)).thenReturn(new ArrayList<>());

        ResponseEntity<GetAuditListSummaryResponse> response = auditResource.getAuditDataSummaryByCorrelationID(correlationID, page, limit);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(auditService, times(1)).getAuditDataByCorrelationID(correlationID, page, limit);
        verifyNoMoreInteractions(auditService);
    }


    @Test
    public void shouldReturnAuditsFromUserIDAndFromDefaultDateRange() {
        when(auditService.getAuditDataByUserID(userID , page, limit)).thenReturn(new ArrayList<>());

        ResponseEntity<GetAuditListResponse> response = auditResource.getAuditDataByUserID(userID , page, limit);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(auditService, times(1)).getAuditDataByUserID(userID, page, limit);
        verifyNoMoreInteractions(auditService);
    }

    @Test
    public void shouldReturnAllAuditsFromUserIDAndFromDateRange() {
        when(auditService.getAuditDataByUserIDByDateRange(userID, fromDate, toDate , page, limit)).thenReturn(new ArrayList<>());

        ResponseEntity<GetAuditListResponse> response = auditResource.getAuditDataByUserIDAndDateRange(userID, fromDate, toDate , page, limit);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(auditService, times(1)).getAuditDataByUserIDByDateRange(userID, fromDate, toDate , page, limit);
        verifyNoMoreInteractions(auditService);
    }

    @Test
    public void shouldReturnAuditSummariesFromUserIDAndDefaultDateRange() {
        when(auditService.getAuditDataByUserID(userID , page, limit)).thenReturn(new ArrayList<>());

        ResponseEntity<GetAuditListSummaryResponse> response = auditResource.getAuditDataSummaryByUserID(userID , page, limit);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(auditService, times(1)).getAuditDataByUserID(userID , page, limit);
        verifyNoMoreInteractions(auditService);
    }

    @Test
    public void shouldReturnAuditSummariesFromUserIDAndDateRange() {
        when(auditService.getAuditDataByUserIDByDateRange(userID, fromDate, toDate , page, limit)).thenReturn(new ArrayList<>());

        ResponseEntity<GetAuditListSummaryResponse> response = auditResource.getAuditDataSummaryByUserIDAndDateRange(userID, fromDate, toDate , page, limit);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(auditService, times(1)).getAuditDataByUserIDByDateRange(userID, fromDate, toDate , page, limit);
        verifyNoMoreInteractions(auditService);
    }
}
