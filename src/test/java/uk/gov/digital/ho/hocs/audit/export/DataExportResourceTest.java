package uk.gov.digital.ho.hocs.audit.export;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.digital.ho.hocs.audit.auditdetails.exception.EntityPermissionException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DataExportResourceTest {

    @Mock
    private ExportService exportService;
    @Mock
    private CustomExportService customExportService;
    @Mock
    private HttpServletResponse response;

    private DataExportResource dataExportResource;

    @Before
    public void before() throws IOException {
        dataExportResource = new DataExportResource(exportService, customExportService);
    }

    @Test
    public void getDataExport() throws IOException {
        LocalDate fromDate = LocalDate.now();
        LocalDate toDate  = LocalDate.now().plusDays(10);
        String caseType = "C1";
        ExportType exportType = ExportType.CASE_DATA;

        ServletOutputStream servletOutputStream = mock(ServletOutputStream.class);
        when(response.getOutputStream()).thenReturn(servletOutputStream);

        dataExportResource.getDataExport(fromDate, toDate, caseType, exportType, false, true, null, null, response);

        verify(response).setContentType("text/csv");
        verify(response).setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=" + caseType.toLowerCase() + "-" + exportType.toString().toLowerCase() + "-" +
                        LocalDate.now().toString() + ".csv");
        verify(response).setStatus(200);
        verify(response).getOutputStream();
        verify(exportService).auditExport(fromDate, toDate, servletOutputStream, caseType, exportType, false, true, null, null);

        checkNoMoreInteractions();
    }

    @Test
    public void getDataExport_OnException() throws IOException {
        LocalDate fromDate = LocalDate.now();
        LocalDate toDate  = LocalDate.now().plusDays(10);
        String caseType = "C1";
        ExportType exportType = ExportType.CASE_DATA;

        ServletOutputStream servletOutputStream = mock(ServletOutputStream.class);
        when(response.getOutputStream()).thenReturn(servletOutputStream);
        doThrow(new IllegalArgumentException("Dummy exception")).when(exportService).auditExport(fromDate, toDate, servletOutputStream, caseType, exportType, false, true, null, null);

        dataExportResource.getDataExport(fromDate, toDate, caseType, exportType, false, true, null, null, response);

        verify(response).setContentType("text/csv");
        verify(response).setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=" + caseType.toLowerCase() + "-" + exportType.toString().toLowerCase() + "-" +
                        LocalDate.now().toString() + ".csv");
        verify(response).setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        verify(response).getOutputStream();
        verify(exportService).auditExport(fromDate, toDate, servletOutputStream, caseType, exportType, false, true, null, null);

        checkNoMoreInteractions();
    }

    @Test
    public void getSomuExport() throws IOException {
        LocalDate fromDate = LocalDate.now();
        LocalDate toDate  = LocalDate.now().plusDays(10);
        String caseType = "C1";
        String somuType = "SomuType";

        ServletOutputStream servletOutputStream = mock(ServletOutputStream.class);
        when(response.getOutputStream()).thenReturn(servletOutputStream);

        dataExportResource.getSomuExport(fromDate, toDate, caseType, somuType, false, null, null, response);

        verify(response).setContentType("text/csv");
        verify(response).setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=" + caseType.toLowerCase() + "-" + somuType + "-" +
                        LocalDate.now().toString() + ".csv");
        verify(response).setStatus(200);
        verify(response).getOutputStream();
        verify(exportService).auditSomuExport(fromDate, toDate, servletOutputStream, caseType, somuType, false, null, null);
        checkNoMoreInteractions();
    }

    @Test
    public void getTopics() throws IOException {

        ServletOutputStream servletOutputStream = mock(ServletOutputStream.class);
        when(response.getOutputStream()).thenReturn(servletOutputStream);

        dataExportResource.getTopics(response, true);

        verify(response).setContentType("text/csv");
        verify(response).setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=topics-" + LocalDate.now().toString() + ".csv");
        verify(response).setStatus(200);
        verify(response).getOutputStream();
        verify(exportService).staticTopicExport(servletOutputStream, true);

        checkNoMoreInteractions();
    }

    @Test
    public void getTopics_OnException() throws IOException {

        ServletOutputStream servletOutputStream = mock(ServletOutputStream.class);
        when(response.getOutputStream()).thenReturn(servletOutputStream);
        doThrow(new IllegalArgumentException("Dummy exception")).when(exportService).staticTopicExport(servletOutputStream, true);

        dataExportResource.getTopics(response, true);

        verify(response).setContentType("text/csv");
        verify(response).setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=topics-" + LocalDate.now().toString() + ".csv");
        verify(response).setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        verify(response).getOutputStream();
        verify(exportService).staticTopicExport(servletOutputStream, true);

        checkNoMoreInteractions();
    }

    @Test
    public void getTeams() throws IOException {

        ServletOutputStream servletOutputStream = mock(ServletOutputStream.class);
        when(response.getOutputStream()).thenReturn(servletOutputStream);

        dataExportResource.getTeams(response, true);

        verify(response).setContentType("text/csv");
        verify(response).setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=teams-" + LocalDate.now().toString() + ".csv");
        verify(response).setStatus(200);
        verify(response).getOutputStream();
        verify(exportService).staticTeamExport(servletOutputStream, true);

        checkNoMoreInteractions();
    }

    @Test
    public void getTeams_OnException() throws IOException {

        ServletOutputStream servletOutputStream = mock(ServletOutputStream.class);
        when(response.getOutputStream()).thenReturn(servletOutputStream);
        doThrow(new IllegalArgumentException("Dummy exception")).when(exportService).staticTeamExport(servletOutputStream, true);

        dataExportResource.getTeams(response, true);

        verify(response).setContentType("text/csv");
        verify(response).setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=teams-" + LocalDate.now().toString() + ".csv");
        verify(response).setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        verify(response).getOutputStream();
        verify(exportService).staticTeamExport(servletOutputStream, true);

        checkNoMoreInteractions();
    }

    @Test
    public void getUnitsForTeams() throws IOException {

        ServletOutputStream servletOutputStream = mock(ServletOutputStream.class);
        when(response.getOutputStream()).thenReturn(servletOutputStream);

        dataExportResource.getUnitsForTeams(response, true);

        verify(response).setContentType("text/csv");
        verify(response).setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=units_teams-" + LocalDate.now().toString() + ".csv");
        verify(response).setStatus(200);
        verify(response).getOutputStream();
        verify(exportService).staticUnitsForTeamsExport(servletOutputStream, true);
        checkNoMoreInteractions();
    }

    @Test
    public void getUsersWithTeams() throws IOException {

        ServletOutputStream servletOutputStream = mock(ServletOutputStream.class);
        when(response.getOutputStream()).thenReturn(servletOutputStream);

        dataExportResource.getUsersWithTeams(response, true);

        verify(response).setContentType("text/csv");
        verify(response).setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=users_teams-" + LocalDate.now().toString() + ".csv");
        verify(response).setStatus(200);
        verify(response).getOutputStream();
        verify(exportService).userTeamsExport(servletOutputStream, true);
        checkNoMoreInteractions();
    }

    @Test
    public void getUsersWithTeams_OnException() throws IOException {

        ServletOutputStream servletOutputStream = mock(ServletOutputStream.class);
        when(response.getOutputStream()).thenReturn(servletOutputStream);
        doThrow(new IllegalArgumentException("Dummy exception")).when(exportService).userTeamsExport(servletOutputStream, true);

        dataExportResource.getUsersWithTeams(response, true);

        verify(response).setContentType("text/csv");
        verify(response).setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=users_teams-" + LocalDate.now().toString() + ".csv");
        verify(response).setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        verify(response).getOutputStream();
        verify(exportService).userTeamsExport(servletOutputStream, true);
        checkNoMoreInteractions();
    }

    @Test
    public void getUnitsForTeams_OnException() throws IOException {

        ServletOutputStream servletOutputStream = mock(ServletOutputStream.class);
        when(response.getOutputStream()).thenReturn(servletOutputStream);
        doThrow(new IllegalArgumentException("Dummy exception")).when(exportService).staticUnitsForTeamsExport(servletOutputStream, true);

        dataExportResource.getUnitsForTeams(response, true);

        verify(response).setContentType("text/csv");
        verify(response).setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=units_teams-" + LocalDate.now().toString() + ".csv");
        verify(response).setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        verify(response).getOutputStream();
        verify(exportService).staticUnitsForTeamsExport(servletOutputStream, true);
        checkNoMoreInteractions();
    }

    @Test
    public void getUsers() throws IOException {

        ServletOutputStream servletOutputStream = mock(ServletOutputStream.class);
        when(response.getOutputStream()).thenReturn(servletOutputStream);

        dataExportResource.getUsers(response, true);

        verify(response).setContentType("text/csv");
        verify(response).setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=users-" + LocalDate.now().toString() + ".csv");
        verify(response).setStatus(200);
        verify(response).getOutputStream();
        verify(exportService).staticUserExport(servletOutputStream, true);

        checkNoMoreInteractions();
    }

    @Test
    public void getUsers_OnException() throws IOException {

        ServletOutputStream servletOutputStream = mock(ServletOutputStream.class);
        when(response.getOutputStream()).thenReturn(servletOutputStream);
        doThrow(new IllegalArgumentException("Dummy exception")).when(exportService).staticUserExport(servletOutputStream, true);

        dataExportResource.getUsers(response, true);

        verify(response).setContentType("text/csv");
        verify(response).setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=users-" + LocalDate.now().toString() + ".csv");
        verify(response).setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        verify(response).getOutputStream();
        verify(exportService).staticUserExport(servletOutputStream, true);

        checkNoMoreInteractions();
    }

    @Test
    public void getCustomDataExport() throws IOException {
        String customReportCode = "Code123";

        dataExportResource.getCustomDataExport(response, customReportCode, true);

        verify(customExportService).customExport(response, customReportCode, true);
        verify(response).setStatus(200);

        checkNoMoreInteractions();
    }


    @Test
    public void getCustomDataExport_HttpClientErrorException() throws IOException {
        String customReportCode = "Code123";

        doThrow(new HttpClientErrorException(HttpStatus.I_AM_A_TEAPOT))
                .when(customExportService)
                .customExport(response, customReportCode, true);
        dataExportResource.getCustomDataExport(response, customReportCode, true);

        verify(customExportService, atLeastOnce()).customExport(response, customReportCode, true);
        verify(response).setStatus(418);
        checkNoMoreInteractions();
    }


    @Test
    public void getCustomDataExport_EntityPermissionException() throws IOException {
        String customReportCode = "Code123";

        doThrow(new EntityPermissionException("Test exception"))
                .when(customExportService)
                .customExport(response, customReportCode, true);
        dataExportResource.getCustomDataExport(response, customReportCode, true);

        verify(customExportService).customExport(response, customReportCode, true);
        verify(response).setStatus(403);
        checkNoMoreInteractions();
    }

    @Test
    public void getCustomDataExport_nonHttpClientErrorException() throws IOException {
        String customReportCode = "Code123";

        doThrow(new IllegalArgumentException("Dummy exception"))
                .when(customExportService)
                .customExport(response, customReportCode, true);
        dataExportResource.getCustomDataExport(response, customReportCode, true);

        verify(customExportService).customExport(response, customReportCode, true);
        verify(response).setStatus(500);
        checkNoMoreInteractions();
    }

    private void checkNoMoreInteractions() {
        verifyNoMoreInteractions(exportService, customExportService, response);
    }


}
