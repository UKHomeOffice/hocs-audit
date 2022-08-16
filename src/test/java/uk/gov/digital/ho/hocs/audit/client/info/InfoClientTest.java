package uk.gov.digital.ho.hocs.audit.client.info;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import uk.gov.digital.ho.hocs.audit.client.info.dto.CaseTypeActionDto;
import uk.gov.digital.ho.hocs.audit.client.info.dto.CaseTypeDto;
import uk.gov.digital.ho.hocs.audit.client.info.dto.SomuTypeDto;
import uk.gov.digital.ho.hocs.audit.client.info.dto.TeamDto;
import uk.gov.digital.ho.hocs.audit.client.info.dto.TopicDto;
import uk.gov.digital.ho.hocs.audit.client.info.dto.TopicTeamDto;
import uk.gov.digital.ho.hocs.audit.client.info.dto.UnitDto;
import uk.gov.digital.ho.hocs.audit.client.info.dto.UserDto;
import uk.gov.digital.ho.hocs.audit.core.RestHelper;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@SpringBootTest
public class InfoClientTest {

    @MockBean
    private RestHelper restHelper;

    private InfoClient infoClient;

    private static final String BASE_URL = "https://base.url";
    private static final String PERMISSION_1 = "permission_name1";
    private static final String PERMISSION_2 = "permission_name2";
    private static final String VIEW_CODE_1 = "view_name1";
    private static final String VIEW_CODE_2 = "view_name2";
    private static final String VIEW_DISPLAY_NAME_1 = "display_name1";
    private static final String VIEW_DISPLAY_NAME_2 = "display_name2";
    private static final String FIELD_NAME_A = "FieldA";
    private static final String FIELD_NAME_B = "FieldB";
    private static final String FIELD_NAME_C = "FieldC";
    private static final String FIELD_NAME_D = "FieldD";


    @BeforeEach
    public void before() {
        infoClient = new InfoClient(restHelper, BASE_URL);
    }

    @Test
    public void getCaseTypes() {
        Set<CaseTypeDto> response = new HashSet<>(Collections.singletonList(new CaseTypeDto("Case Type 1", "C1", "C")));
        when(restHelper.get(eq(BASE_URL), eq("/caseType"), any(ParameterizedTypeReference.class))).thenReturn(response);
        Set<CaseTypeDto> results = infoClient.getCaseTypes();

        assertEquals(response, results);
        verify(restHelper).get(eq(BASE_URL), eq("/caseType"), any(ParameterizedTypeReference.class));

        verifyNoMoreInteractions(restHelper);
    }


    @Test
    public void getUsers() {
        Set<UserDto> response = new HashSet<>(Collections.singletonList(new UserDto("1", "user1", "Bill", "Smith", "bill.smith@email.com")));
        when(restHelper.get(eq(BASE_URL), eq("/users"), any(ParameterizedTypeReference.class))).thenReturn(response);
        Set<UserDto> results = infoClient.getUsers();

        assertEquals(response, results);
        verify(restHelper).get(eq(BASE_URL), eq("/users"), any(ParameterizedTypeReference.class));

        verifyNoMoreInteractions(restHelper);
    }

    @Test
    public void getSomuType() {
        SomuTypeDto response = new SomuTypeDto(UUID.randomUUID(), "caseType", "somuType", "{}", true);
        when(restHelper.get(eq(BASE_URL), eq("/somuType/caseType/somuType"), eq(SomuTypeDto.class))).thenReturn(response);

        SomuTypeDto result = infoClient.getSomuType("caseType", "somuType");

        assertEquals(response, result);
        verify(restHelper).get(eq(BASE_URL), eq("/somuType/caseType/somuType"), eq(SomuTypeDto.class));
        verifyNoMoreInteractions(restHelper);
    }

    @Test
    public void getTopics() {
        Set<TopicDto> response = new HashSet<>(Collections.singletonList(new TopicDto("Topic text", UUID.randomUUID(), true)));
        when(restHelper.get(eq(BASE_URL), eq("/topics"), any(ParameterizedTypeReference.class))).thenReturn(response);
        Set<TopicDto> results = infoClient.getTopics();

        assertEquals(response, results);
        verify(restHelper).get(eq(BASE_URL), eq("/topics"), any(ParameterizedTypeReference.class));

        verifyNoMoreInteractions(restHelper);
    }

    @Test
    public void getTopicsWithTeams() {
        Set<TopicTeamDto> response = new HashSet<>(Collections.singletonList(new TopicTeamDto("Topic text", UUID.randomUUID(), null)));
        when(restHelper.get(eq(BASE_URL), eq("/topics/TEST/teams"), any(ParameterizedTypeReference.class))).thenReturn(response);

        Set<TopicTeamDto> results = infoClient.getTopicsWithTeams("TEST");

        assertEquals(response, results);
        verify(restHelper).get(eq(BASE_URL), eq("/topics/TEST/teams"), any(ParameterizedTypeReference.class));
        verifyNoMoreInteractions(restHelper);
    }

    @Test
    public void getTeams() {
        Set<TeamDto> response = new HashSet<>(Collections.singletonList(new TeamDto("Team text", UUID.randomUUID(), true, null)));
        when(restHelper.get(eq(BASE_URL), eq("/team"), any(ParameterizedTypeReference.class))).thenReturn(response);
        Set<TeamDto> results = infoClient.getTeams();

        assertEquals(response, results);
        verify(restHelper).get(eq(BASE_URL), eq("/team"), any(ParameterizedTypeReference.class));

        verifyNoMoreInteractions(restHelper);
    }

    @Test
    public void getAllTeams() {
        Set<TeamDto> response = new HashSet<>(Collections.singletonList(new TeamDto("Team text", UUID.randomUUID(), false, null)));
        when(restHelper.get(eq(BASE_URL), eq("/team/all"), any(ParameterizedTypeReference.class))).thenReturn(response);
        Set<TeamDto> results = infoClient.getAllTeams();

        assertEquals(response, results);
        verify(restHelper).get(eq(BASE_URL), eq("/team/all"), any(ParameterizedTypeReference.class));

        verifyNoMoreInteractions(restHelper);
    }

    @Test
    public void getTeamsForUnit() {
        String unitUUID = UUID.randomUUID().toString();
        Set<TeamDto> response = new HashSet<>(Collections.singletonList(new TeamDto("Team text", UUID.randomUUID(), true, null)));
        when(restHelper.get(eq(BASE_URL), eq("/unit/" + unitUUID + "/teams"), any(ParameterizedTypeReference.class))).thenReturn(response);

        Set<TeamDto> results = infoClient.getTeamsForUnit(unitUUID);

        assertEquals(response, results);
        verify(restHelper).get(eq(BASE_URL), eq("/unit/" + unitUUID + "/teams"), any(ParameterizedTypeReference.class));
        verifyNoMoreInteractions(restHelper);
    }

    @Test
    public void getTeam() {
        UUID teamUUID = UUID.randomUUID();
        TeamDto response = new TeamDto("Team text", teamUUID, true, null);
        when(restHelper.get(eq(BASE_URL), eq("/team/" + teamUUID), eq(TeamDto.class))).thenReturn(response);
        TeamDto result = infoClient.getTeam(teamUUID.toString());

        assertEquals(response, result);
        verify(restHelper).get(eq(BASE_URL), eq("/team/" + teamUUID), eq(TeamDto.class));

        verifyNoMoreInteractions(restHelper);
    }


    @Test
    public void getUnitByTeam() {
        String unitUUID = UUID.randomUUID().toString();
        UnitDto response = new UnitDto("Unit diplay name", unitUUID, "UnitA");
        when(restHelper.get(eq(BASE_URL), eq("/team/" + unitUUID + "/unit"), eq(UnitDto.class))).thenReturn(response);
        UnitDto result = infoClient.getUnitByTeam(unitUUID);

        assertEquals(response, result);
        verify(restHelper).get(eq(BASE_URL), eq("/team/" + unitUUID + "/unit"), eq(UnitDto.class));

        verifyNoMoreInteractions(restHelper);
    }

    @Test
    public void getUnits() {
        Set<UnitDto> response = new HashSet<>(Collections.singletonList(new UnitDto("Unit text", UUID.randomUUID().toString(), "U1")));
        when(restHelper.get(eq(BASE_URL), eq("/unit"), any(ParameterizedTypeReference.class))).thenReturn(response);
        Set<UnitDto> results = infoClient.getUnits();

        assertEquals(response, results);
        verify(restHelper).get(eq(BASE_URL), eq("/unit"), any(ParameterizedTypeReference.class));

        verifyNoMoreInteractions(restHelper);
    }

    @Test
    public void getCaseTypeActions_shouldReturnListOfActions() {
        CaseTypeActionDto caseTypeAction1 = new CaseTypeActionDto(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "CT1",
                "ACTION_1",
                "ACTION_LABEL_1",
                1,
                10,
                true,
                "{}"
        );
        CaseTypeActionDto caseTypeAction2 = new CaseTypeActionDto(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "CT2",
                "ACTION_2",
                "ACTION_LABEL_2",
                1,
                20,
                true,
                "{}"
        );

        List<CaseTypeActionDto> caseTypeActionsList = new LinkedList<>();
        caseTypeActionsList.add(caseTypeAction1);
        caseTypeActionsList.add(caseTypeAction2);
        when(restHelper.get(eq(BASE_URL), eq("/caseType/actions"), any(ParameterizedTypeReference.class))).thenReturn(caseTypeActionsList);


        List<CaseTypeActionDto> results = infoClient.getCaseTypeActions();

        assertEquals(caseTypeActionsList, results);
        verify(restHelper).get(eq(BASE_URL), eq("/caseType/actions"), any(ParameterizedTypeReference.class));

        verifyNoMoreInteractions(restHelper);
    }

}
