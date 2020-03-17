package uk.gov.digital.ho.hocs.audit.export;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.audit.export.caseworkclient.CaseworkClient;
import uk.gov.digital.ho.hocs.audit.export.infoclient.ExportViewConstants;
import uk.gov.digital.ho.hocs.audit.export.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.audit.export.infoclient.dto.ExportViewDto;
import uk.gov.digital.ho.hocs.audit.export.infoclient.dto.ExportViewFieldAdapterDto;
import uk.gov.digital.ho.hocs.audit.export.infoclient.dto.ExportViewFieldDto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(MockitoJUnitRunner.class)
public class CustomExportDataConverterTest {

    private static final String PERMISSION_1 = "permission_name1";
    private static final String VIEW_CODE_1 = "view_name1";
    private static final String VIEW_DISPLAY_NAME_1 = "display_name1";
    private static final String FIELD_NAME_A = "FieldA";
    private static final String FIELD_NAME_B = "FieldB";

    @Mock
    private InfoClient infoClient;
    @Mock
    private CaseworkClient caseworkClient;

    private CustomExportDataConverter converter;


    @Before
    public void before() {
        converter = new CustomExportDataConverter(infoClient, caseworkClient);
    }

    @Test
    public void getHeaders() {
        ExportViewDto exportViewDto = buildExportView1();


        List<String> results = converter.getHeaders(exportViewDto);

        assertThat(results).isNotNull();
        assertThat(results.size()).isEqualTo(2);
        assertThat(results).contains(FIELD_NAME_A, FIELD_NAME_B);

    }

    @Test
    public void getHeaders_Hidden() {
        ExportViewDto exportViewDto = buildExportView2();


        List<String> results = converter.getHeaders(exportViewDto);

        assertThat(results).isNotNull();
        assertThat(results.size()).isEqualTo(1);
        assertThat(results).contains(FIELD_NAME_A);

    }

    private ExportViewDto buildExportView1() {
        ExportViewFieldDto fieldA = new ExportViewFieldDto(1L, 1L, 1L, FIELD_NAME_A, new ArrayList<>());
        ExportViewFieldDto fieldB = new ExportViewFieldDto(2L, 1L, 2L, FIELD_NAME_B, new ArrayList<>());
        List<ExportViewFieldDto> fields1 = new ArrayList<>(Arrays.asList(fieldA, fieldB));
        return new ExportViewDto(1L, VIEW_CODE_1, VIEW_DISPLAY_NAME_1, PERMISSION_1, fields1);
    }

    private ExportViewDto buildExportView2() {
        ExportViewFieldAdapterDto hiddenAdapter = new ExportViewFieldAdapterDto(1L, 1L, 1L, ExportViewConstants.FIELD_ADAPTER_HIDDEN);
        ExportViewFieldDto fieldA = new ExportViewFieldDto(1L, 1L, 1L, FIELD_NAME_A, new ArrayList<>());
        ExportViewFieldDto fieldB = new ExportViewFieldDto(2L, 1L, 2L, FIELD_NAME_B, Arrays.asList(hiddenAdapter));
        List<ExportViewFieldDto> fields1 = new ArrayList<>(Arrays.asList(fieldA, fieldB));
        return new ExportViewDto(1L, VIEW_CODE_1, VIEW_DISPLAY_NAME_1, PERMISSION_1, fields1);
    }
}
