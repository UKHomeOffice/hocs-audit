package uk.gov.digital.ho.hocs.audit.export;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.http.HttpHeaders;
import uk.gov.digital.ho.hocs.audit.application.RequestData;
import uk.gov.digital.ho.hocs.audit.auditdetails.exception.EntityPermissionException;
import uk.gov.digital.ho.hocs.audit.auditdetails.repository.AuditRepository;
import uk.gov.digital.ho.hocs.audit.export.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.audit.export.infoclient.dto.ExportViewDto;
import uk.gov.digital.ho.hocs.audit.export.infoclient.dto.ExportViewFieldDto;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CustomExportServiceTest {

    private static final String PERMISSION_1 = "permission_name1";
    private static final String VIEW_CODE_1 = "view_name1";
    private static final String VIEW_DISPLAY_NAME_1 = "display_name1";
    private static final String FIELD_NAME_A = "FieldA";
    private static final String FIELD_NAME_B = "FieldB";

    @Mock
    private AuditRepository auditRepository;
    @Mock
    private InfoClient infoClient;
    @Mock
    private CustomExportDataConverter customExportDataConverter;
    @Mock
    private RequestData requestData;
    @Mock
    private HttpServletResponse servletResponse;
    @Mock
    private HeaderConverter passThroughHeaderConverter;

    private CustomExportService customExportService;

    @Before
    public void before() {
        when(passThroughHeaderConverter.substitute(anyList())).thenAnswer(new Answer<List<String>>() {
            @Override
            public List<String> answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                return (List<String>) args[0];
            }
        });
        customExportService = new CustomExportService(auditRepository, infoClient, customExportDataConverter, passThroughHeaderConverter, requestData);
    }

    @Test(expected = EntityPermissionException.class)
    public void customExport_NoPermission() throws IOException {
        ExportViewDto exportViewDto = buildExportView1();
        when(infoClient.getExportView(VIEW_CODE_1)).thenReturn(exportViewDto);
        when(requestData.roles()).thenReturn(new ArrayList<>());

        customExportService.customExport(servletResponse, VIEW_CODE_1, true);

        checkNoMoreInteractions();
    }

    @Test()
    public void customExport() throws IOException {
        Stream<Object[]> inputData = buildInputData();
        List<Object[]> outputData = buildOutputData();
        ExportViewDto exportViewDto = buildExportView1();
        when(infoClient.getExportView(VIEW_CODE_1)).thenReturn(exportViewDto);
        when(requestData.roles()).thenReturn(new ArrayList<>(Collections.singletonList(PERMISSION_1)));
        when(auditRepository.getResultsFromView(VIEW_CODE_1)).thenReturn(inputData);
        when(servletResponse.getOutputStream()).thenReturn(mock(ServletOutputStream.class));
        when(customExportDataConverter.getHeaders(exportViewDto)).thenReturn(Arrays.asList("Header1", "Header2", "Header3"));

        customExportService.customExport(servletResponse, VIEW_CODE_1, true);

        verify(servletResponse).setContentType("text/csv;charset=UTF-8");
        verify(servletResponse).setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=" + VIEW_DISPLAY_NAME_1 + "-" + LocalDate.now().toString() + ".csv");
        verify(servletResponse).getOutputStream();
        verify(infoClient).getExportView(VIEW_CODE_1);
        verify(auditRepository).getResultsFromView(VIEW_CODE_1);
        verify(customExportDataConverter, times(2)).convertData(any(), any());
        verify(customExportDataConverter).initialiseAdapters();
        verify(requestData).roles();
        verify(customExportDataConverter).getHeaders(exportViewDto);
        verify(passThroughHeaderConverter).substitute(anyList());

        checkNoMoreInteractions();
    }

    private Stream<Object[]> buildInputData() {
        List<Object[]> inputData = new ArrayList<>();
        Object[] row1 = new Object[3];
        Object[] row2 = new Object[3];
        row1[0] = "Row1Col1";
        row1[1] = "Row1Col2";
        row1[2] = "Row1Col3";
        row2[0] = "Row2Col1";
        row2[1] = "Row2Col2";
        row2[2] = "Row2Col3";
        inputData.add(row1);
        inputData.add(row2);
        return inputData.stream();
    }

    private List<Object[]> buildOutputData() {
        List<Object[]> inputData = new ArrayList<>();
        Object[] row1 = new Object[3];
        Object[] row2 = new Object[3];
        row1[0] = "Row1Col1Converted";
        row1[1] = "Row1Col2Converted";
        row1[2] = "Row1Col3Converted";
        row2[0] = "Row2Col1Converted";
        row2[1] = "Row2Col2Converted";
        row2[2] = "Row2Col3Converted";
        inputData.add(row1);
        inputData.add(row2);
        return inputData;
    }


    private void checkNoMoreInteractions() {
        verifyNoMoreInteractions(auditRepository, infoClient, customExportDataConverter, requestData, servletResponse);
    }

    private ExportViewDto buildExportView1() {
        ExportViewFieldDto fieldA = new ExportViewFieldDto(1L, 1L, 1L, FIELD_NAME_A, new ArrayList<>());
        ExportViewFieldDto fieldB = new ExportViewFieldDto(2L, 1L, 2L, FIELD_NAME_B, new ArrayList<>());
        List<ExportViewFieldDto> fields1 = new ArrayList<>(Arrays.asList(fieldA, fieldB));
        return new ExportViewDto(1L, VIEW_CODE_1, VIEW_DISPLAY_NAME_1, PERMISSION_1, fields1);
    }


}
