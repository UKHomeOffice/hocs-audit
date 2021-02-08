package uk.gov.digital.ho.hocs.audit.export;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.audit.export.caseworkclient.CaseworkClient;
import uk.gov.digital.ho.hocs.audit.export.caseworkclient.dto.GetCaseReferenceResponse;
import uk.gov.digital.ho.hocs.audit.export.caseworkclient.dto.GetCorrespondentOutlineResponse;
import uk.gov.digital.ho.hocs.audit.export.caseworkclient.dto.GetTopicResponse;
import uk.gov.digital.ho.hocs.audit.export.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.audit.export.infoclient.dto.EntityDataDto;
import uk.gov.digital.ho.hocs.audit.export.infoclient.dto.EntityDto;
import uk.gov.digital.ho.hocs.audit.export.infoclient.dto.TeamDto;
import uk.gov.digital.ho.hocs.audit.export.infoclient.dto.UnitDto;
import uk.gov.digital.ho.hocs.audit.export.infoclient.dto.UserDto;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExportDataConverterTest {

    private static final String CASE_ID = UUID.randomUUID().toString();
    private static final String CASE_ID_NONE = UUID.randomUUID().toString();
    private static final String USER1_ID = UUID.randomUUID().toString();
    private static final String USER1_USERNAME = "user-Jim";
    private static final String USER1_FIRST_NAME = "Jim";
    private static final String USER1_LAST_NAME = "Smith";
    private static final String USER1_EMAIL = USER1_FIRST_NAME + "." + USER1_LAST_NAME + "@mail.com";
    private static final String UNIT1_ID = UUID.randomUUID().toString();
    private static final String UNIT1_DISPLAY_NAME = "Unit 1";
    private static final String UNIT1_SHORT_CODE = "U1";
    private static final String TOPIC1_ID = UUID.randomUUID().toString();
    private static final String TOPIC1_TEXT = "Topic 1";
    private static final String TEAM1_ID = UUID.randomUUID().toString();
    private static final String TEAM1_DISPLAY_NAME = "Team 1";
    private static final String CORR1_ID = UUID.randomUUID().toString();
    private static final String CORR1_FULLNAME = "Bob Smith";
    private static final String CASE_REF = "REF/1234567/890";
    private static final String CASE_REF_NONE = "";
    private static final String CASE_TYPE_SHORT_CODE = "x1";
    private static final String ENTITY_1_SIMPLE_NAME = "aaaa_bbbb_cccc";
    private static final String ENTITY_1_TITLE = "aaaa bbbb / (cccc)";
    private static final String ENTITY_2_SIMPLE_NAME = "dddd_eeee_ffff";
    private static final String ENTITY_2_TITLE_WITH_COMMAS = "dddd, eeee, (ffff)";

    @Mock
    private InfoClient infoClient;
    @Mock
    private CaseworkClient caseworkClient;

    private ExportDataConverter converter;

    @Before
    public void before() {

        if (converter == null) {
            when(infoClient.getUsers()).thenReturn(buildUsers());
            when(infoClient.getAllTeams()).thenReturn(buildTeams());
            when(infoClient.getUnits()).thenReturn(buildUnits());
            when(infoClient.getEntitiesForList("MPAM_ENQUIRY_SUBJECTS")).thenReturn(buildMpamEnquirySubjects());
            when(caseworkClient.getAllCaseTopics()).thenReturn(buildTopics());
            when(caseworkClient.getAllActiveCorrespondents()).thenReturn(buildCorrespondents());
            when(caseworkClient.getCaseReference(CASE_ID)).thenReturn(new GetCaseReferenceResponse(UUID.fromString(CASE_ID), CASE_REF));
            when(caseworkClient.getCaseReference(CASE_ID_NONE)).thenReturn(new GetCaseReferenceResponse(UUID.fromString(CASE_ID_NONE), CASE_REF_NONE));
        }

        converter = new ExportDataConverter(infoClient, caseworkClient);
        converter.initialise();
    }

    @Test
    public void testUuidRegex() {
        String uuid = UUID.randomUUID().toString();
        assertThat(converter.isUUID(uuid)).isTrue();
        assertThat(converter.isUUID(uuid.replace('-','1'))).isFalse();
    }

    @Test
    public void convertDataHandlesEmptyArray() {

        String[] testResult = converter.convertData(new String[] {}, CASE_TYPE_SHORT_CODE);

        assertThat(testResult).isNotNull();
        assertThat(testResult.length).isEqualTo(0);
    }

    @Test
    public void convertDataWhenNothingToConvertThenNothingConverted() {

        String[] testData = { "a", "b", "c", "d" };

        String[] testResult = converter.convertData(testData, CASE_TYPE_SHORT_CODE);

        assertThat(testResult).isNotNull();
        assertThat(testResult.length).isEqualTo(4);
        assertThat(testResult[0]).isEqualTo(testData[0]);
        assertThat(testResult[1]).isEqualTo(testData[1]);
        assertThat(testResult[2]).isEqualTo(testData[2]);
        assertThat(testResult[3]).isEqualTo(testData[3]);
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

    private Set<UserDto> buildUsers() {
        Set<UserDto> users = new HashSet<>();
        users.add(new UserDto(USER1_ID, USER1_USERNAME, USER1_FIRST_NAME, USER1_LAST_NAME, USER1_EMAIL));
        return users;
    }

    private Set<TeamDto> buildTeams() {
        Set<TeamDto> teams = new HashSet<>();
        teams.add(new TeamDto(TEAM1_DISPLAY_NAME, UUID.fromString(TEAM1_ID), true, UNIT1_ID));
        return teams;
    }

    private Set<UnitDto> buildUnits() {
        Set<UnitDto> units = new HashSet<>();
        units.add(new UnitDto(UNIT1_DISPLAY_NAME, UNIT1_ID, UNIT1_SHORT_CODE));
        return units;
    }

    private Set<GetTopicResponse> buildTopics() {
        Set<GetTopicResponse> topics = new HashSet<>();
        topics.add(new GetTopicResponse(UUID.fromString(TOPIC1_ID), LocalDateTime.now(), UUID.randomUUID(), TOPIC1_TEXT, UUID.randomUUID()));
        return topics;
    }

    private Set<GetCorrespondentOutlineResponse> buildCorrespondents() {
        Set<GetCorrespondentOutlineResponse> correspondents = new HashSet<>();
        correspondents.add(new GetCorrespondentOutlineResponse(UUID.fromString(CORR1_ID), CORR1_FULLNAME));
        return correspondents;
    }

    private Set<EntityDto> buildMpamEnquirySubjects() {
        Set<EntityDto> entitySet = new HashSet<>();
        entitySet.add(new EntityDto(1l, UUID.randomUUID(), ENTITY_1_SIMPLE_NAME, new EntityDataDto(ENTITY_1_TITLE), UUID.randomUUID(), true));
        entitySet.add(new EntityDto(1l, UUID.randomUUID(), ENTITY_2_SIMPLE_NAME, new EntityDataDto(ENTITY_2_TITLE_WITH_COMMAS), UUID.randomUUID(), true));
        return entitySet;
    }
}
