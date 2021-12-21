package uk.gov.digital.ho.hocs.audit.export;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.digital.ho.hocs.audit.export.caseworkclient.CaseworkClient;
import uk.gov.digital.ho.hocs.audit.export.caseworkclient.dto.GetTopicResponse;
import uk.gov.digital.ho.hocs.audit.export.infoclient.ExportViewConstants;
import uk.gov.digital.ho.hocs.audit.export.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.audit.export.infoclient.dto.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class CustomExportDataConverterTest {
    private static final String PERMISSION_1 = "permission_name1";
    private static final String VIEW_CODE_1 = "view_name1";
    private static final String VIEW_DISPLAY_NAME_1 = "display_name1";
    private static final String FIELD_NAME_A = "FieldA";
    private static final String FIELD_NAME_B = "FieldB";
    private static final String FIELD_NAME_C = "FieldC";
    private static final String FIELD_NAME_D = "FieldD";
    private static final String FIELD_NAME_E = "FieldE";
    private static final String FIELD_NAME_F = "FieldF";
    private static final String FIELD_NAME_G = "FieldG";

    private static final String USER1_ID = UUID.randomUUID().toString();
    private static final String USER1_USERNAME = "user-Bob";
    private static final String USER1_FIRST_NAME = "Bob";
    private static final String USER1_LAST_NAME = "Smith";
    private static final String USER1_EMAIL = USER1_FIRST_NAME + "-" + USER1_LAST_NAME + "@mail.com";
    private static final String USER2_ID = UUID.randomUUID().toString();
    private static final String USER2_USERNAME = "user-Adam";
    private static final String USER2_FIRST_NAME = "Adam";
    private static final String USER2_LAST_NAME = "Baker";
    private static final String USER2_EMAIL = USER2_FIRST_NAME + "-" + USER2_LAST_NAME + "@mail.com";
    private static final String UNIT1_ID = UUID.randomUUID().toString();
    private static final String UNIT1_DISPLAY_NAME = "Unit 1";
    private static final String UNIT1_SHORT_CODE = "U1";
    private static final String UNIT2_ID = UUID.randomUUID().toString();
    private static final String UNIT2_DISPLAY_NAME = "Unit 2";
    private static final String UNIT2_SHORT_CODE = "U2";
    private static final String TOPIC1_ID = UUID.randomUUID().toString();
    private static final String TOPIC1_TEXT = "Random Topic 1";
    private static final String TOPIC2_ID = UUID.randomUUID().toString();
    private static final String TOPIC2_TEXT = "Random Topic 2";
    private static final String TEAM1_ID = UUID.randomUUID().toString();
    private static final String TEAM1_DISPLAY_NAME = "Team 1";
    private static final String TEAM2_ID = UUID.randomUUID().toString();
    private static final String TEAM2_DISPLAY_NAME = "Team 2";

    @Mock
    private InfoClient infoClient;
    @Mock
    private CaseworkClient caseworkClient;

    private CustomExportDataConverter converter;

    @BeforeEach
    public void before() {
        Set<UserDto> users = buildUsers();
        Set<TeamDto> teams = buildTeams();
        Set<UnitDto> units = buildUnits();
        Set<GetTopicResponse> topics = buildTopics();

        when(infoClient.getUsers()).thenReturn(users);
        when(infoClient.getAllTeams()).thenReturn(teams);
        when(infoClient.getUnits()).thenReturn(units);
        when(caseworkClient.getAllCaseTopics()).thenReturn(topics);

        converter = new CustomExportDataConverter(infoClient, caseworkClient);
        converter.initialiseAdapters();
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

    @Test
    public void convertData() {
        ExportViewDto exportViewDto = buildExportView3();
        Stream<Object[]> input = buildInputData();

        List<Object[]> results = input
                .map(result -> converter.convertData(result, exportViewDto.getFields()))
                .collect(Collectors.toList());

        assertThat(results).isNotNull();
        assertThat(results.size()).isEqualTo(2);
        assertThat(results.get(0)).isNotNull();
        assertThat(results.get(0).length).isEqualTo(7);
        assertThat(results.get(0)[0]).isEqualTo(USER1_EMAIL);
        assertThat(results.get(0)[1]).isEqualTo(USER1_USERNAME);
        assertThat(results.get(0)[2]).isEqualTo(USER1_FIRST_NAME + " " + USER1_LAST_NAME);
        assertThat(results.get(0)[3]).isEqualTo(UNIT1_DISPLAY_NAME);
        assertThat(results.get(0)[4]).isEqualTo(TOPIC1_TEXT);
        assertThat(results.get(0)[5]).isEqualTo(TEAM1_DISPLAY_NAME);
        assertThat(results.get(0)[6]).isEqualTo("Data 1");
        assertThat(results.get(1)).isNotNull();
        assertThat(results.get(1).length).isEqualTo(7);
        assertThat(results.get(1)[0]).isEqualTo(USER2_EMAIL);
        assertThat(results.get(1)[1]).isEqualTo(USER2_USERNAME);
        assertThat(results.get(1)[2]).isEqualTo(USER2_FIRST_NAME + " " + USER2_LAST_NAME);
        assertThat(results.get(1)[3]).isEqualTo(UNIT2_DISPLAY_NAME);
        assertThat(results.get(1)[4]).isEqualTo(TOPIC2_TEXT);
        assertThat(results.get(1)[5]).isEqualTo(TEAM2_DISPLAY_NAME);
        assertThat(results.get(1)[6]).isEqualTo("Data 2");

        verify(infoClient).getUsers();
        verify(infoClient).getAllTeams();
        verify(infoClient).getUnits();
        verify(caseworkClient).getAllCaseTopics();

        verifyNoMoreInteractions(infoClient, caseworkClient);

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

    private ExportViewDto buildExportView3() {
        ExportViewFieldAdapterDto userEmailAdapter = new ExportViewFieldAdapterDto(null, null, null, ExportViewConstants.FIELD_ADAPTER_USER_EMAIL);
        ExportViewFieldAdapterDto usernameAdapter = new ExportViewFieldAdapterDto(null, null, null, ExportViewConstants.FIELD_ADAPTER_USERNAME);
        ExportViewFieldAdapterDto userFirstAndLastName = new ExportViewFieldAdapterDto(null, null, null, ExportViewConstants.FIELD_ADAPTER_FIRST_AND_LAST_NAME);
        ExportViewFieldAdapterDto unitNameAdapter = new ExportViewFieldAdapterDto(null, null, null, ExportViewConstants.FIELD_ADAPTER_UNIT_NAME);
        ExportViewFieldAdapterDto topicNameAdapter = new ExportViewFieldAdapterDto(null, null, null, ExportViewConstants.FIELD_ADAPTER_TOPIC_NAME);
        ExportViewFieldAdapterDto teamNameAdapter = new ExportViewFieldAdapterDto(null, null, null, ExportViewConstants.FIELD_ADAPTER_TEAM_NAME);

        ExportViewFieldDto fieldA = new ExportViewFieldDto(1L, 1L, 1L, FIELD_NAME_A, Collections.singletonList(userEmailAdapter));
        ExportViewFieldDto fieldB = new ExportViewFieldDto(2L, 1L, 2L, FIELD_NAME_B, Collections.singletonList(usernameAdapter));
        ExportViewFieldDto fieldC = new ExportViewFieldDto(3L, 1L, 3L, FIELD_NAME_C, Collections.singletonList(userFirstAndLastName));
        ExportViewFieldDto fieldD = new ExportViewFieldDto(4L, 1L, 4L, FIELD_NAME_D, Collections.singletonList(unitNameAdapter));
        ExportViewFieldDto fieldE = new ExportViewFieldDto(5L, 1L, 5L, FIELD_NAME_E, Collections.singletonList(topicNameAdapter));
        ExportViewFieldDto fieldF = new ExportViewFieldDto(6L, 1L, 6L, FIELD_NAME_F, Collections.singletonList(teamNameAdapter));
        ExportViewFieldDto fieldG = new ExportViewFieldDto(7L, 1L, 7L, FIELD_NAME_G, new ArrayList<>());
        List<ExportViewFieldDto> fields1 = new ArrayList<>(Arrays.asList(fieldA, fieldB, fieldC, fieldD, fieldE, fieldF, fieldG));
        return new ExportViewDto(1L, VIEW_CODE_1, VIEW_DISPLAY_NAME_1, PERMISSION_1, fields1);
    }

    private Set<UserDto> buildUsers() {
        Set<UserDto> users = new HashSet<>();
        users.add(new UserDto(USER1_ID, USER1_USERNAME, USER1_FIRST_NAME, USER1_LAST_NAME, USER1_EMAIL));
        users.add(new UserDto(USER2_ID, USER2_USERNAME, USER2_FIRST_NAME, USER2_LAST_NAME, USER2_EMAIL));
        return users;
    }

    private Set<TeamDto> buildTeams() {
        Set<TeamDto> teams = new HashSet<>();
        teams.add(new TeamDto(TEAM1_DISPLAY_NAME, UUID.fromString(TEAM1_ID), true, UNIT1_ID));
        teams.add(new TeamDto(TEAM2_DISPLAY_NAME, UUID.fromString(TEAM2_ID), true, UNIT2_ID));
        return teams;
    }

    private Set<UnitDto> buildUnits() {
        Set<UnitDto> units = new HashSet<>();
        units.add(new UnitDto(UNIT1_DISPLAY_NAME, UNIT1_ID, UNIT1_SHORT_CODE));
        units.add(new UnitDto(UNIT2_DISPLAY_NAME, UNIT2_ID, UNIT2_SHORT_CODE));
        return units;
    }

    private Set<GetTopicResponse> buildTopics() {
        Set<GetTopicResponse> topics = new HashSet<>();
        topics.add(new GetTopicResponse(UUID.fromString(TOPIC1_ID), LocalDateTime.now(), UUID.randomUUID(), TOPIC1_TEXT, UUID.randomUUID()));
        topics.add(new GetTopicResponse(UUID.fromString(TOPIC2_ID), LocalDateTime.now(), UUID.randomUUID(), TOPIC2_TEXT, UUID.randomUUID()));

        return topics;
    }


    private Stream<Object[]> buildInputData() {
        List<Object[]> inputData = new ArrayList<>();
        Object[] row1 = new Object[7];
        Object[] row2 = new Object[7];
        row1[0] = USER1_ID;
        row1[1] = USER1_ID;
        row1[2] = USER1_ID;
        row1[3] = TEAM1_ID;
        row1[4] = TOPIC1_ID;
        row1[5] = TEAM1_ID;
        row1[6] = "Data 1";

        row2[0] = USER2_ID;
        row2[1] = USER2_ID;
        row2[2] = USER2_ID;
        row2[3] = TEAM2_ID;
        row2[4] = TOPIC2_ID;
        row2[5] = TEAM2_ID;
        row2[6] = "Data 2";

        inputData.add(row1);
        inputData.add(row2);

        return inputData.stream();
    }
}
