package uk.gov.digital.ho.hocs.audit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.hocs.audit.auditdetails.dto.*;
import uk.gov.digital.ho.hocs.audit.auditdetails.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.audit.auditdetails.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.audit.auditdetails.model.AuditData;

import java.time.LocalDateTime;
import java.util.ArrayList;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AuditResourceTest {

    @Mock
    private AuditDataService auditService;

    private AuditDataResource auditResource;
    private String correlationID, raisingService, auditPayload, namespace, type, userID;
    private LocalDateTime auditTimestamp;
    private AuditData validAudit;

    private CreateAuditDto request;

    private String toDate;
    private String fromDate;
    private int page;
    private int limit;

    @Before
    public void setUp() {

        correlationID = "correlationID1";
        raisingService = "raisingServiceName";
        auditPayload = "";
        namespace = "namespaceEventOccurredIn";
        auditTimestamp = LocalDateTime.now();
        type = "eventAuditType";
        userID = "userXYZ";

        request = new CreateAuditDto(correlationID, raisingService, auditPayload, namespace, auditTimestamp, type, userID);
        validAudit = new AuditData(correlationID, raisingService, auditPayload, namespace, auditTimestamp, type, userID);

        auditResource = new AuditDataResource(auditService);

        fromDate = "2018-08-02";
        toDate = "2018-11-02";

        page = 5;
        limit = 10;
    }

    @Test
    public void shouldCreateAuditWithValidParams() throws EntityCreationException {

        when(auditService.createAudit(correlationID,
                raisingService,
                auditPayload,
                namespace,
                auditTimestamp,
                type,
                userID)).thenReturn(validAudit);

        ResponseEntity response = auditResource.createAudit(request);

        verify(auditService, times(1)).createAudit(correlationID,
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
    public void shouldReturnAllAuditsFromDefaultDateRange() throws EntityNotFoundException {
        when(auditService.getAuditDataList(page, limit)).thenReturn(new ArrayList<>());

        ResponseEntity<GetAuditListResponse> response = auditResource.getAudits(page, limit);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(auditService, times(1)).getAuditDataList(page, limit);
        verifyNoMoreInteractions(auditService);
    }

    @Test
    public void shouldReturnAllAuditsFromDateRange() throws EntityNotFoundException {
        when(auditService.getAuditDataByDateRange(fromDate,toDate, page, limit)).thenReturn(new ArrayList<>());

        ResponseEntity<GetAuditListResponse> response = auditResource.getAuditDataByDateRange(fromDate,toDate, page, limit);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(auditService, times(1)).getAuditDataByDateRange(fromDate,toDate, page, limit);
        verifyNoMoreInteractions(auditService);
    }

    @Test
    public void shouldReturnAllAuditSummariesFromDefaultDateRange() throws EntityNotFoundException {
        when(auditService.getAuditDataList(page, limit)).thenReturn(new ArrayList<>());

        ResponseEntity<GetAuditListSummaryResponse> response = auditResource.getAuditsSummary(page, limit);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(auditService, times(1)).getAuditDataList(page, limit);
        verifyNoMoreInteractions(auditService);
    }

    @Test
    public void shouldReturnAllAuditSummariesFromDateRange() throws EntityNotFoundException {
        when(auditService.getAuditDataByDateRange(fromDate,toDate, page, limit)).thenReturn(new ArrayList<>());

        ResponseEntity<GetAuditListSummaryResponse> response = auditResource.getAuditDataSummaryByDateRange(fromDate,toDate, page, limit);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(auditService, times(1)).getAuditDataByDateRange(fromDate,toDate, page, limit);
        verifyNoMoreInteractions(auditService);
    }



    @Test
    public void shouldReturnAuditForUUID() throws EntityNotFoundException {
        when(auditService.getAuditDataByUUID(any())).thenReturn(validAudit);

        ResponseEntity<GetAuditResponse> response = auditResource.getAudit(any());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(auditService, times(1)).getAuditDataByUUID(any());
        verifyNoMoreInteractions(auditService);
    }

    @Test
    public void shouldReturnAuditSummaryForUUID() throws EntityNotFoundException {
        when(auditService.getAuditDataByUUID(any())).thenReturn(validAudit);

        ResponseEntity<GetAuditSummaryResponse> response = auditResource.getAuditSummary(any());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(auditService, times(1)).getAuditDataByUUID(any());
        verifyNoMoreInteractions(auditService);
    }




    @Test
    public void shouldReturnAuditsFromCorrelationIDAndFromDefaultDateRange() throws EntityNotFoundException {
        when(auditService.getAuditDataByCorrelationID(correlationID, page, limit)).thenReturn(new ArrayList<>());

        ResponseEntity<GetAuditListResponse> response = auditResource.getAuditDataByCorrelationID(correlationID, page, limit);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(auditService, times(1)).getAuditDataByCorrelationID(correlationID, page, limit);
        verifyNoMoreInteractions(auditService);
    }

    @Test
    public void shouldReturnAllAuditsFromCorrelationIDAndFromDateRange() throws EntityNotFoundException {
        when(auditService.getAuditDataByCorrelationIDByDateRange(correlationID, fromDate, toDate, page, limit)).thenReturn(new ArrayList<>());

        ResponseEntity<GetAuditListResponse> response = auditResource.getAuditDataByCorrelationIDAndDateRange(correlationID, fromDate, toDate , page, limit);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(auditService, times(1)).getAuditDataByCorrelationIDByDateRange(correlationID, fromDate, toDate , page, limit);
        verifyNoMoreInteractions(auditService);
    }

    @Test
    public void shouldReturnAuditSummariesFromCorrelationIDAndDefaultDateRange() throws EntityNotFoundException {
        when(auditService.getAuditDataByCorrelationID(correlationID, page, limit)).thenReturn(new ArrayList<>());

        ResponseEntity<GetAuditListSummaryResponse> response = auditResource.getAuditDataSummaryByCorrelationID(correlationID, page, limit);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(auditService, times(1)).getAuditDataByCorrelationID(correlationID, page, limit);
        verifyNoMoreInteractions(auditService);
    }

    @Test
    public void shouldReturnAuditSummariesFromCorrelationIDAndDateRange() throws EntityNotFoundException {
        when(auditService.getAuditDataByCorrelationIDByDateRange(correlationID, fromDate, toDate , page, limit)).thenReturn(new ArrayList<>());

        ResponseEntity<GetAuditListSummaryResponse> response = auditResource.getAuditDataSummaryByCorrelationIDAndDateRange(correlationID, fromDate, toDate , page, limit);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(auditService, times(1)).getAuditDataByCorrelationIDByDateRange(correlationID, fromDate, toDate , page, limit);
        verifyNoMoreInteractions(auditService);
    }




    @Test
    public void shouldReturnAuditsFromUserIDAndFromDefaultDateRange() throws EntityNotFoundException {
        when(auditService.getAuditDataByUserID(userID , page, limit)).thenReturn(new ArrayList<>());

        ResponseEntity<GetAuditListResponse> response = auditResource.getAuditDataByUserID(userID , page, limit);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(auditService, times(1)).getAuditDataByUserID(userID, page, limit);
        verifyNoMoreInteractions(auditService);
    }

    @Test
    public void shouldReturnAllAuditsFromUserIDAndFromDateRange() throws EntityNotFoundException {
        when(auditService.getAuditDataByUserIDByDateRange(userID, fromDate, toDate , page, limit)).thenReturn(new ArrayList<>());

        ResponseEntity<GetAuditListResponse> response = auditResource.getAuditDataByUserIDAndDateRange(userID, fromDate, toDate , page, limit);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(auditService, times(1)).getAuditDataByUserIDByDateRange(userID, fromDate, toDate , page, limit);
        verifyNoMoreInteractions(auditService);
    }

    @Test
    public void shouldReturnAuditSummariesFromUserIDAndDefaultDateRange() throws EntityNotFoundException {
        when(auditService.getAuditDataByUserID(userID , page, limit)).thenReturn(new ArrayList<>());

        ResponseEntity<GetAuditListSummaryResponse> response = auditResource.getAuditDataSummaryByUserID(userID , page, limit);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(auditService, times(1)).getAuditDataByUserID(userID , page, limit);
        verifyNoMoreInteractions(auditService);
    }

    @Test
    public void shouldReturnAuditSummariesFromUserIDAndDateRange() throws EntityNotFoundException {
        when(auditService.getAuditDataByUserIDByDateRange(userID, fromDate, toDate , page, limit)).thenReturn(new ArrayList<>());

        ResponseEntity<GetAuditListSummaryResponse> response = auditResource.getAuditDataSummaryByUserIDAndDateRange(userID, fromDate, toDate , page, limit);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(auditService, times(1)).getAuditDataByUserIDByDateRange(userID, fromDate, toDate , page, limit);
        verifyNoMoreInteractions(auditService);
    }
}
