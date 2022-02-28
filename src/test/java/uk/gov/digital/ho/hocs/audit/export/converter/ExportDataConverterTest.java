package uk.gov.digital.ho.hocs.audit.export.converter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.audit.export.caseworkclient.CaseworkClient;
import uk.gov.digital.ho.hocs.audit.export.caseworkclient.dto.GetCaseReferenceResponse;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExportDataConverterTest {

    private static final String CASE_ID = UUID.randomUUID().toString();
    private static final String CASE_ID_NONE = UUID.randomUUID().toString();
    private static final String USER1_ID = UUID.randomUUID().toString();
    private static final String USER1_USERNAME = "user-Jim";
    private static final String UNIT1_ID = UUID.randomUUID().toString();
    private static final String AUDIT_RECORD_ID = UUID.randomUUID().toString();
    private static final String UNIT1_DISPLAY_NAME = "Unit 1";
    private static final String TOPIC1_ID = UUID.randomUUID().toString();
    private static final String TOPIC1_TEXT = "Topic 1";
    private static final String TEAM1_ID = UUID.randomUUID().toString();
    private static final String TEAM1_DISPLAY_NAME = "Team 1";
    private static final String CORR1_ID = UUID.randomUUID().toString();
    private static final String CORR1_FULLNAME = "Bob Smith";
    private static final String CASE_REF = "REF/1234567/890";
    private static final String CASE_REF_NONE = "";
    private static final String REFERENCE_NOT_FOUND = "REFERENCE NOT FOUND";
    private static final String CASE_TYPE_SHORT_CODE = "x1";
    private static final String ENTITY_1_SIMPLE_NAME = "aaaa_bbbb_cccc";
    private static final String ENTITY_1_TITLE = "aaaa bbbb / (cccc)";
    private static final String ENTITY_2_SIMPLE_NAME = "dddd_eeee_ffff";
    private static final String ENTITY_2_TITLE_WITH_COMMAS = "dddd, eeee, (ffff)";

    private static final Map<String, String> UUID_TO_NAME = buildUuidToNameMap();
    private static final Map<String, String> MPAM_CODE_TO_NAME = buildMpamCodeToNameMap();

    @Mock
    private CaseworkClient caseworkClient;

    private ExportDataConverter converter;

    @Before
    public void before() {
        when(caseworkClient.getCaseReference(CASE_ID)).thenReturn(new GetCaseReferenceResponse(UUID.fromString(CASE_ID), CASE_REF));
        when(caseworkClient.getCaseReference(CASE_ID_NONE)).thenReturn(new GetCaseReferenceResponse(UUID.fromString(CASE_ID_NONE), CASE_REF_NONE));
        when(caseworkClient.getCaseReference(AUDIT_RECORD_ID)).thenReturn(new GetCaseReferenceResponse(UUID.fromString(CASE_ID_NONE), REFERENCE_NOT_FOUND));

        converter = new ExportDataConverter(UUID_TO_NAME, MPAM_CODE_TO_NAME, caseworkClient);
    }

    @Test
    public void convertDataHandlesEmptyArray() {

        String[] testResult = converter.convertData(new String[] {}, CASE_TYPE_SHORT_CODE);

        assertThat(testResult).isNotNull();
        assertThat(testResult.length).isEqualTo(0);
    }

    @Test
    public void convertDataWhenNothingToConvertThenNothingConverted() {

        String[] testData = { "a", "b", "c", "d", AUDIT_RECORD_ID};

        String[] testResult = converter.convertData(testData, CASE_TYPE_SHORT_CODE);

        assertThat(testResult).isNotNull();
        assertThat(testResult.length).isEqualTo(5);
        assertThat(testResult[0]).isEqualTo(testData[0]);
        assertThat(testResult[1]).isEqualTo(testData[1]);
        assertThat(testResult[2]).isEqualTo(testData[2]);
        assertThat(testResult[3]).isEqualTo(testData[3]);
        assertThat(testResult[4]).isEqualTo(testData[4]);
    }

    @Test
    public void convertDataWhenUserUuidThenUsername() {

        String[] testData = { USER1_ID, "b", "c", "d" };

        String[] testResult = converter.convertData(testData, CASE_TYPE_SHORT_CODE);

        assertThat(testResult).isNotNull();
        assertThat(testResult.length).isEqualTo(4);
        assertThat(testResult[0]).isEqualTo(USER1_USERNAME);
        assertThat(testResult[1]).isEqualTo(testData[1]);
        assertThat(testResult[2]).isEqualTo(testData[2]);
        assertThat(testResult[3]).isEqualTo(testData[3]);
    }

    @Test
    public void convertDataWhenUnitUuidThenUnitDisplayName() {

        String[] testData = { "a", UNIT1_ID, "c", "d" };

        String[] testResult = converter.convertData(testData, CASE_TYPE_SHORT_CODE);

        assertThat(testResult).isNotNull();
        assertThat(testResult.length).isEqualTo(4);
        assertThat(testResult[0]).isEqualTo(testData[0]);
        assertThat(testResult[1]).isEqualTo(UNIT1_DISPLAY_NAME);
        assertThat(testResult[2]).isEqualTo(testData[2]);
        assertThat(testResult[3]).isEqualTo(testData[3]);
    }

    @Test
    public void convertDataWhenTopicUuidThenTopicText() {

        String[] testData = { "a", "b", TOPIC1_ID, "d" };

        String[] testResult = converter.convertData(testData, CASE_TYPE_SHORT_CODE);

        assertThat(testResult).isNotNull();
        assertThat(testResult.length).isEqualTo(4);
        assertThat(testResult[0]).isEqualTo(testData[0]);
        assertThat(testResult[1]).isEqualTo(testData[1]);
        assertThat(testResult[2]).isEqualTo(TOPIC1_TEXT);
        assertThat(testResult[3]).isEqualTo(testData[3]);
    }

    @Test
    public void convertDataWhenTeamUuidThenTeamDisplayName() {

        String[] testData = { "a", "b", "c", TEAM1_ID };

        String[] testResult = converter.convertData(testData, CASE_TYPE_SHORT_CODE);

        assertThat(testResult).isNotNull();
        assertThat(testResult.length).isEqualTo(4);
        assertThat(testResult[0]).isEqualTo(testData[0]);
        assertThat(testResult[1]).isEqualTo(testData[1]);
        assertThat(testResult[2]).isEqualTo(testData[2]);
        assertThat(testResult[3]).isEqualTo(TEAM1_DISPLAY_NAME);
    }

    @Test
    public void convertDataWhenCorrespondentUuidThenCorrespondentFullname() {

        String[] testData = { "a", CORR1_ID, "c", "d" };

        String[] testResult = converter.convertData(testData, CASE_TYPE_SHORT_CODE);

        assertThat(testResult).isNotNull();
        assertThat(testResult.length).isEqualTo(4);
        assertThat(testResult[0]).isEqualTo(testData[0]);
        assertThat(testResult[1]).isEqualTo(CORR1_FULLNAME);
        assertThat(testResult[2]).isEqualTo(testData[2]);
        assertThat(testResult[3]).isEqualTo(testData[3]);
    }

    @Test
    public void convertDataWhenUuidsThenAllReplaced() {

        String[] testData = { USER1_ID, UNIT1_ID, TOPIC1_ID, TEAM1_ID, CORR1_ID };

        String[] testResult = converter.convertData(testData, CASE_TYPE_SHORT_CODE);

        assertThat(testResult).isNotNull();
        assertThat(testResult.length).isEqualTo(5);
        assertThat(testResult[0]).isEqualTo(USER1_USERNAME);
        assertThat(testResult[1]).isEqualTo(UNIT1_DISPLAY_NAME);
        assertThat(testResult[2]).isEqualTo(TOPIC1_TEXT);
        assertThat(testResult[3]).isEqualTo(TEAM1_DISPLAY_NAME);
        assertThat(testResult[4]).isEqualTo(CORR1_FULLNAME);
    }


    @Test
    public void convertCaseRefLookup() {

        String[] testData = { CASE_ID };

        String[] testResult = converter.convertData(testData, CASE_TYPE_SHORT_CODE);

        assertThat(testResult).isNotNull();
        assertThat(testResult.length).isEqualTo(1);
        assertThat(testResult[0]).isEqualTo(CASE_REF);
        verify(caseworkClient).getCaseReference(CASE_ID);
    }

    @Test
    public void convertCaseRefNonLookup() {

        String[] testData = { CASE_ID_NONE };

        String[] testResult = converter.convertData(testData, CASE_TYPE_SHORT_CODE);

        assertThat(testResult).isNotNull();
        assertThat(testResult.length).isEqualTo(1);
        assertThat(testResult[0]).isEqualTo(CASE_ID_NONE); // leaves the data as is
        verify(caseworkClient).getCaseReference(CASE_ID_NONE);

    }

    @Test
    public void convertMpamEntityCode() {

        String[] testData = { ENTITY_1_SIMPLE_NAME, "b", "c", "d" };

        String[] testResult = converter.convertData(testData, "b5");

        assertThat(testResult).isNotNull();
        assertThat(testResult.length).isEqualTo(4);
        assertThat(testResult[0]).isEqualTo("aaaa bbbb / (cccc)");
        assertThat(testResult[1]).isEqualTo(testData[1]);
        assertThat(testResult[2]).isEqualTo(testData[2]);
        assertThat(testResult[3]).isEqualTo(testData[3]);
    }

    @Test
    public void convertMpamEntityCodeWhenTitleContainsCommasThenTitleIsSanitised() {

        String[] testData = { ENTITY_2_SIMPLE_NAME, "b", "c", "d" };

        String[] testResult = converter.convertData(testData, "b5");

        assertThat(testResult).isNotNull();
        assertThat(testResult.length).isEqualTo(4);
        assertThat(testResult[0]).isEqualTo("dddd eeee (ffff)");
        assertThat(testResult[1]).isEqualTo(testData[1]);
        assertThat(testResult[2]).isEqualTo(testData[2]);
        assertThat(testResult[3]).isEqualTo(testData[3]);
    }

    @Test
    public void convertMpamEntityCodeWhenNotMpamThenNoConversion() {

        String[] testData = { ENTITY_1_SIMPLE_NAME, "b", "c", "d" };

        String[] testResult = converter.convertData(testData, CASE_TYPE_SHORT_CODE);

        assertThat(testResult).isNotNull();
        assertThat(testResult.length).isEqualTo(4);
        assertThat(testResult[0]).isEqualTo(ENTITY_1_SIMPLE_NAME);
        assertThat(testResult[1]).isEqualTo(testData[1]);
        assertThat(testResult[2]).isEqualTo(testData[2]);
        assertThat(testResult[3]).isEqualTo(testData[3]);
    }

    @Test
    public void convertMpamEntityCodeWhenCodeNotFoundThenNoConversion() {

        String invalidCode = "simple_name2";
        String[] testData = { invalidCode, "b", "c", "d" };

        String[] testResult = converter.convertData(testData, "b5");

        assertThat(testResult).isNotNull();
        assertThat(testResult.length).isEqualTo(4);
        assertThat(testResult[0]).isEqualTo(invalidCode);
        assertThat(testResult[1]).isEqualTo(testData[1]);
        assertThat(testResult[2]).isEqualTo(testData[2]);
        assertThat(testResult[3]).isEqualTo(testData[3]);
    }

    private static Map<String, String> buildUuidToNameMap() {
        return new HashMap<>(
                Map.ofEntries(
                    new AbstractMap.SimpleEntry<>(USER1_ID, USER1_USERNAME),
                    new AbstractMap.SimpleEntry<>(TEAM1_ID, TEAM1_DISPLAY_NAME),
                    new AbstractMap.SimpleEntry<>(UNIT1_ID, UNIT1_DISPLAY_NAME),
                    new AbstractMap.SimpleEntry<>(TOPIC1_ID, TOPIC1_TEXT),
                    new AbstractMap.SimpleEntry<>(CORR1_ID, CORR1_FULLNAME))
        );
    }

    private static Map<String, String> buildMpamCodeToNameMap() {
        return new HashMap<>(Map.ofEntries(
                new AbstractMap.SimpleEntry<>(ENTITY_1_SIMPLE_NAME, ENTITY_1_TITLE),
                new AbstractMap.SimpleEntry<>(ENTITY_2_SIMPLE_NAME, ENTITY_2_TITLE_WITH_COMMAS))
        );
    }
}
