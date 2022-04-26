package uk.gov.digital.ho.hocs.audit.entrypoint;

import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.hocs.audit.client.info.dto.TeamDto;
import uk.gov.digital.ho.hocs.audit.client.info.dto.TopicDto;
import uk.gov.digital.ho.hocs.audit.client.info.dto.UnitDto;
import uk.gov.digital.ho.hocs.audit.client.info.dto.UserDto;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.mockito.BDDMockito.given;
import static org.springframework.http.HttpMethod.GET;

public class StaticExportResourceTest extends BaseExportResourceTest {

    @Test
    public void topicExportTest() throws IOException {
        TopicDto topicDto = new TopicDto("TEST1", UUID.randomUUID(), true);

        given(infoClient.getTopics()).willReturn(Set.of(topicDto));

        ResponseEntity<String> result = restTemplate.exchange(getExportUri("/export/topics"),
                GET, HttpEntity.EMPTY, String.class);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());
        Assertions.assertEquals(getFileName("topics"), result.getHeaders().getContentDisposition().getFilename());

        var rows = getCSVRows(result.getBody())
                .stream()
                .map(CSVRecord::toList).collect(Collectors.toList());
        var expectedRows = List.of(
                List.of(topicDto.getValue().toString(), topicDto.getLabel(), "true"));

        Assertions.assertEquals(2, rows.size());
        Assertions.assertTrue(rows.containsAll(expectedRows));
    }

    @Test
    public void teamExportTest() throws IOException {
        TeamDto teamDto = new TeamDto("TEST1", UUID.randomUUID(), true, UUID.randomUUID().toString());

        given(infoClient.getTeams()).willReturn(Set.of(teamDto));

        ResponseEntity<String> result = restTemplate.exchange(getExportUri("/export/teams"),
                GET, HttpEntity.EMPTY, String.class);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());
        Assertions.assertEquals(getFileName("teams"), result.getHeaders().getContentDisposition().getFilename());

        var rows = getCSVRows(result.getBody())
                .stream()
                .map(CSVRecord::toList).collect(Collectors.toList());
        var expectedRows = List.of(
                List.of(teamDto.getUuid().toString(), teamDto.getDisplayName()));

        Assertions.assertEquals(2, rows.size());
        Assertions.assertTrue(rows.containsAll(expectedRows));
    }

    @Test
    public void unitTeamExportTest() throws IOException {
        UnitDto unitDto = new UnitDto("TEST_UNIT", UUID.randomUUID().toString(), "1");
        UnitDto unit2Dto = new UnitDto("TEST2_UNIT", UUID.randomUUID().toString(), "2");
        TeamDto teamDto = new TeamDto("TEST1", UUID.randomUUID(), true, unitDto.getUuid());

        given(infoClient.getUnits()).willReturn(Set.of(unitDto, unit2Dto));
        given(infoClient.getTeamsForUnit(unitDto.getUuid())).willReturn(Set.of(teamDto));

        ResponseEntity<String> result = restTemplate.exchange(getExportUri("/export/units/teams"),
                GET, HttpEntity.EMPTY, String.class);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());
        Assertions.assertEquals(getFileName("units_teams"), result.getHeaders().getContentDisposition().getFilename());

        var rows = getCSVRows(result.getBody())
                .stream()
                .map(CSVRecord::toList).collect(Collectors.toList());
        var expectedRows = List.of(
                List.of(unitDto.getUuid(), unitDto.getDisplayName(), teamDto.getUuid().toString(), teamDto.getDisplayName()),
                List.of(unit2Dto.getUuid(), unit2Dto.getDisplayName(), "", ""));

        Assertions.assertTrue(rows.containsAll(expectedRows));
    }

    @Test
    public void userExportTest() throws IOException {
        UserDto userDto =
                new UserDto(UUID.randomUUID().toString(), "Username", "First", "Last", "test@example.com");

        given(infoClient.getUsers()).willReturn(Set.of(userDto));

        ResponseEntity<String> result = restTemplate.exchange(getExportUri("/export/users"),
                GET, HttpEntity.EMPTY, String.class);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());
        Assertions.assertEquals(getFileName("users"), result.getHeaders().getContentDisposition().getFilename());

        var rows = getCSVRows(result.getBody())
                .stream()
                .map(CSVRecord::toList).collect(Collectors.toList());
        var expectedRows = List.of(
                List.of(userDto.getId(), userDto.getUsername(), userDto.getFirstName(), userDto.getLastName(), userDto.getEmail())
        );

        Assertions.assertTrue(rows.containsAll(expectedRows));
    }

}
