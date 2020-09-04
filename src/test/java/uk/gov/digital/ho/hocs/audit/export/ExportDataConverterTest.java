package uk.gov.digital.ho.hocs.audit.export;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.audit.export.caseworkclient.CaseworkClient;
import uk.gov.digital.ho.hocs.audit.export.caseworkclient.dto.GetCorrespondentOutlineResponse;
import uk.gov.digital.ho.hocs.audit.export.caseworkclient.dto.GetTopicResponse;
import uk.gov.digital.ho.hocs.audit.export.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.audit.export.infoclient.dto.TeamDto;
import uk.gov.digital.ho.hocs.audit.export.infoclient.dto.UnitDto;
import uk.gov.digital.ho.hocs.audit.export.infoclient.dto.UserDto;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExportDataConverterTest {

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

    @Mock
    private InfoClient infoClient;
    @Mock
    private CaseworkClient caseworkClient;

    private ExportDataConverter converter;

    @Before
    public void before() {

        if (converter == null) {
            when(infoClient.getUsers()).thenReturn(buildUsers());
            when(infoClient.getTeams()).thenReturn(buildTeams());
            when(infoClient.getUnits()).thenReturn(buildUnits());
            when(caseworkClient.getAllCaseTopics()).thenReturn(buildTopics());
            when(caseworkClient.getAllActiveCorrespondents()).thenReturn(buildCorrespondents());
        }

        converter = new ExportDataConverter(infoClient, caseworkClient);
    }

    @Test
    public void convertDataHandlesEmptyArray() {

        String[] testResult = converter.convertData(new String[] {});

        assertThat(testResult).isNotNull();
        assertThat(testResult.length).isEqualTo(0);
    }

    @Test
    public void convertDataWhenNothingToConvertThenNothingConverted() {

        String[] testData = { "a", "b", "c", "d" };

        String[] testResult = converter.convertData(testData);

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

        String[] testResult = converter.convertData(testData);

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

        String[] testResult = converter.convertData(testData);

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

        String[] testResult = converter.convertData(testData);

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

        String[] testResult = converter.convertData(testData);

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

        String[] testResult = converter.convertData(testData);

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

        String[] testResult = converter.convertData(testData);

        assertThat(testResult).isNotNull();
        assertThat(testResult.length).isEqualTo(5);
        assertThat(testResult[0]).isEqualTo(USER1_USERNAME);
        assertThat(testResult[1]).isEqualTo(UNIT1_DISPLAY_NAME);
        assertThat(testResult[2]).isEqualTo(TOPIC1_TEXT);
        assertThat(testResult[3]).isEqualTo(TEAM1_DISPLAY_NAME);
        assertThat(testResult[4]).isEqualTo(CORR1_FULLNAME);
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
}
