package uk.gov.digital.ho.hocs.audit.export.infoclient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import uk.gov.digital.ho.hocs.audit.export.RestHelper;
import uk.gov.digital.ho.hocs.audit.export.infoclient.dto.*;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InfoClientTest {


    @Mock
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

        assertThat(results).isEqualTo(response);
        verify(restHelper).get(eq(BASE_URL), eq("/caseType"), any(ParameterizedTypeReference.class));

        verifyNoMoreInteractions(restHelper);
    }


    @Test
    public void getUsers() {
        Set<UserDto> response = new HashSet<>(Collections.singletonList(new UserDto("1", "user1", "Bill", "Smith", "bill.smith@email.com")));
        when(restHelper.get(eq(BASE_URL), eq("/users"), any(ParameterizedTypeReference.class))).thenReturn(response);
        Set<UserDto> results = infoClient.getUsers();

        assertThat(results).isEqualTo(response);
        verify(restHelper).get(eq(BASE_URL), eq("/users"), any(ParameterizedTypeReference.class));

        verifyNoMoreInteractions(restHelper);
    }

    @Test
    public void getSomuType() {
        SomuTypeDto response = new SomuTypeDto(UUID.randomUUID(), "caseType", "somuType", "{}", true);
        when(restHelper.get(eq(BASE_URL), eq("/somuType/caseType/somuType"), any(ParameterizedTypeReference.class))).thenReturn(response);

        SomuTypeDto result = infoClient.getSomuType("caseType", "somuType");

        assertThat(result).isEqualTo(response);
        verify(restHelper).get(eq(BASE_URL), eq("/somuType/caseType/somuType"), any(ParameterizedTypeReference.class));
        verifyNoMoreInteractions(restHelper);
    }

    @Test
    public void getTopics() {
        Set<TopicDto> response = new HashSet<>(Collections.singletonList(new TopicDto("Topic text", UUID.randomUUID(), true)));
        when(restHelper.get(eq(BASE_URL), eq("/topics"), any(ParameterizedTypeReference.class))).thenReturn(response);
        Set<TopicDto> results = infoClient.getTopics();

        assertThat(results).isEqualTo(response);
        verify(restHelper).get(eq(BASE_URL), eq("/topics"), any(ParameterizedTypeReference.class));

        verifyNoMoreInteractions(restHelper);
    }

    @Test
    public void getTopicsWithTeams() {
        Set<TopicTeamDto> response = new HashSet<>(Collections.singletonList(new TopicTeamDto("Topic text", UUID.randomUUID(), null)));
        when(restHelper.get(eq(BASE_URL), eq("/topics/TEST/teams"), any(ParameterizedTypeReference.class))).thenReturn(response);

        Set<TopicTeamDto> results = infoClient.getTopicsWithTeams("TEST");

        assertThat(results).isEqualTo(response);
        verify(restHelper).get(eq(BASE_URL), eq("/topics/TEST/teams"), any(ParameterizedTypeReference.class));
        verifyNoMoreInteractions(restHelper);
    }

    @Test
    public void getTeams() {
        Set<TeamDto> response = new HashSet<>(Collections.singletonList(new TeamDto("Team text", UUID.randomUUID(), true, null)));
        when(restHelper.get(eq(BASE_URL), eq("/team"), any(ParameterizedTypeReference.class))).thenReturn(response);
        Set<TeamDto> results = infoClient.getTeams();

        assertThat(results).isEqualTo(response);
        verify(restHelper).get(eq(BASE_URL), eq("/team"), any(ParameterizedTypeReference.class));

        verifyNoMoreInteractions(restHelper);
    }

    @Test
    public void getAllTeams() {
        Set<TeamDto> response = new HashSet<>(Collections.singletonList(new TeamDto("Team text", UUID.randomUUID(), false, null)));
        when(restHelper.get(eq(BASE_URL), eq("/team/all"), any(ParameterizedTypeReference.class))).thenReturn(response);
        Set<TeamDto> results = infoClient.getAllTeams();

        assertThat(results).isEqualTo(response);
        verify(restHelper).get(eq(BASE_URL), eq("/team/all"), any(ParameterizedTypeReference.class));

        verifyNoMoreInteractions(restHelper);
    }

    @Test
    public void getTeamsForUnit() {
        String unitUUID = UUID.randomUUID().toString();
        Set<TeamDto> response = new HashSet<>(Collections.singletonList(new TeamDto("Team text", UUID.randomUUID(), true, null)));
        when(restHelper.get(eq(BASE_URL), eq("/unit/" + unitUUID + "/teams"), any(ParameterizedTypeReference.class))).thenReturn(response);

        Set<TeamDto> results = infoClient.getTeamsForUnit(unitUUID);

        assertThat(results).isEqualTo(response);
        verify(restHelper).get(eq(BASE_URL), eq("/unit/" + unitUUID + "/teams"), any(ParameterizedTypeReference.class));
        verifyNoMoreInteractions(restHelper);
    }

    @Test
    public void getTeam() {
        UUID teamUUID = UUID.randomUUID();
        TeamDto response = new TeamDto("Team text", teamUUID, true, null);
        when(restHelper.get(eq(BASE_URL), eq("/team/" + teamUUID), any(ParameterizedTypeReference.class))).thenReturn(response);
        TeamDto result = infoClient.getTeam(teamUUID.toString());

        assertThat(result).isEqualTo(response);
        verify(restHelper).get(eq(BASE_URL), eq("/team/" + teamUUID), any(ParameterizedTypeReference.class));

        verifyNoMoreInteractions(restHelper);
    }


    @Test
    public void getUnitByTeam() {
        String unitUUID = UUID.randomUUID().toString();
        UnitDto response = new UnitDto("Unit diplay name", unitUUID, "UnitA");
        when(restHelper.get(eq(BASE_URL), eq("/team/" + unitUUID + "/unit"), any(ParameterizedTypeReference.class))).thenReturn(response);
        UnitDto result = infoClient.getUnitByTeam(unitUUID);

        assertThat(result).isEqualTo(response);
        verify(restHelper).get(eq(BASE_URL), eq("/team/" + unitUUID + "/unit"), any(ParameterizedTypeReference.class));

        verifyNoMoreInteractions(restHelper);
    }

    @Test
    public void getUnits() {
        Set<UnitDto> response = new HashSet<>(Collections.singletonList(new UnitDto("Unit text", UUID.randomUUID().toString(), "U1")));
        when(restHelper.get(eq(BASE_URL), eq("/unit"), any(ParameterizedTypeReference.class))).thenReturn(response);
        Set<UnitDto> results = infoClient.getUnits();

        assertThat(results).isEqualTo(response);
        verify(restHelper).get(eq(BASE_URL), eq("/unit"), any(ParameterizedTypeReference.class));

        verifyNoMoreInteractions(restHelper);
    }

    @Test
    public void getCaseExportFields() {
        String caseType = "C1";
        Set<String> response = new LinkedHashSet<>(Arrays.asList("ExportField1", "ExportField2"));
        when(restHelper.get(eq(BASE_URL), eq("/schema/caseType/" + caseType + "/reporting"), any(ParameterizedTypeReference.class))).thenReturn(response);
        Set<String> results = infoClient.getCaseExportFields(caseType);

        assertThat(results).isEqualTo(response);
        verify(restHelper).get(eq(BASE_URL), eq("/schema/caseType/" + caseType + "/reporting"), any(ParameterizedTypeReference.class));

        verifyNoMoreInteractions(restHelper);
    }

    @Test
    public void getExportViews() {
        List<ExportViewDto> response = buildExportViews();
        when(restHelper.get(eq(BASE_URL), eq("/export"), any(ParameterizedTypeReference.class))).thenReturn(response);
        List<ExportViewDto> results = infoClient.getExportViews();

        assertThat(results).isEqualTo(response);
        verify(restHelper).get(eq(BASE_URL), eq("/export"), any(ParameterizedTypeReference.class));

        verifyNoMoreInteractions(restHelper);
    }

    @Test
    public void getExportView() {
        ExportViewDto response = buildExportView1();
        when(restHelper.get(eq(BASE_URL), eq("/export/" + VIEW_CODE_1), any(ParameterizedTypeReference.class))).thenReturn(response);
        ExportViewDto results = infoClient.getExportView(VIEW_CODE_1);

        assertThat(results).isEqualTo(response);
        verify(restHelper).get(eq(BASE_URL), eq("/export/" + VIEW_CODE_1), any(ParameterizedTypeReference.class));

        verifyNoMoreInteractions(restHelper);
    }

    @Test
    public void getExportView_nullResult() {
        when(restHelper.get(eq(BASE_URL), eq("/export/" + VIEW_CODE_1), any(ParameterizedTypeReference.class))).thenReturn(null);
        ExportViewDto results = infoClient.getExportView(VIEW_CODE_1);

        assertThat(results).isNull();
        verify(restHelper).get(eq(BASE_URL), eq("/export/" + VIEW_CODE_1), any(ParameterizedTypeReference.class));

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

        assertThat(results).isEqualTo(caseTypeActionsList);
        verify(restHelper).get(eq(BASE_URL), eq("/caseType/actions"), any(ParameterizedTypeReference.class));

        verifyNoMoreInteractions(restHelper);
    }


    private List<ExportViewDto> buildExportViews() {
        return new ArrayList<>(Arrays.asList(buildExportView1(), buildExportView2()));
    }

    private ExportViewDto buildExportView1() {
        ExportViewFieldDto fieldA = new ExportViewFieldDto(1L, 1L, 1L, FIELD_NAME_A, null);
        ExportViewFieldDto fieldB = new ExportViewFieldDto(2L, 1L, 2L, FIELD_NAME_B, null);
        List<ExportViewFieldDto> fields1 = new ArrayList<>(Arrays.asList(fieldA, fieldB));
        return new ExportViewDto(1L, VIEW_CODE_1, VIEW_DISPLAY_NAME_1, PERMISSION_1, fields1);
    }

    private ExportViewDto buildExportView2() {
        ExportViewFieldDto fieldC = new ExportViewFieldDto(3L, 2L, 1L, FIELD_NAME_C, null);
        ExportViewFieldDto fieldD = new ExportViewFieldDto(4L, 2L, 2L, FIELD_NAME_D, null);
        List<ExportViewFieldDto> fields2 = new ArrayList<>(Arrays.asList(fieldC, fieldD));
        return new ExportViewDto(2L, VIEW_CODE_2, VIEW_DISPLAY_NAME_2, PERMISSION_2, fields2);
    }


}
